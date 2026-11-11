package com.tecsup.despensa.ui.lista

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tecsup.despensa.data.repository.ItemRepository
import com.tecsup.despensa.data.repository.ProductosRepository
import com.tecsup.despensa.domain.model.Item
import com.tecsup.despensa.domain.model.Producto
import com.tecsup.despensa.notification.Notificador
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException
import java.text.Normalizer
import java.util.Locale

enum class OrdenLista(val etiqueta: String) {
    NOMBRE("A - Z"),
    PRECIO_MENOR("Menor precio"),
    PRECIO_MAYOR("Mayor precio")
}

data class ListaUiState(
    val cargando: Boolean = true,
    val error: String? = null,
    val catalogo: List<Item> = emptyList(),
    val propios: List<Producto> = emptyList(),
    val categorias: List<String> = emptyList(),
    val consulta: String = "",
    val categoria: String? = null,
    val orden: OrdenLista = OrdenLista.NOMBRE,
    val totalCatalogo: Int = 0,
    val totalPropios: Int = 0
) {
    val sinResultados: Boolean
        get() = !cargando && error == null && catalogo.isEmpty() && propios.isEmpty()
}

private data class Filtros(
    val consulta: String = "",
    val categoria: String? = null,
    val orden: OrdenLista = OrdenLista.NOMBRE
)

/**
 * Catalogo de la despensa: mezcla el catalogo remoto (Room) con los productos
 * propios creados por el administrador (Firestore) y aplica busqueda, filtro
 * por categoria y ordenamiento en memoria.
 */
class ListaViewModel(
    private val itemRepository: ItemRepository,
    private val productosRepository: ProductosRepository,
    private val notificador: Notificador
) : ViewModel() {

    private val _catalogo = MutableStateFlow<List<Item>>(emptyList())
    private val _propios = MutableStateFlow<List<Producto>>(emptyList())
    private val _filtros = MutableStateFlow(Filtros())
    private val _cargando = MutableStateFlow(true)
    private val _error = MutableStateFlow<String?>(null)

    private val _aviso = MutableStateFlow<String?>(null)
    val aviso: StateFlow<String?> = _aviso.asStateFlow()

    val estado: StateFlow<ListaUiState> = combine(
        _catalogo,
        _propios,
        _filtros,
        _cargando,
        _error
    ) { catalogo, propios, filtros, cargando, error ->
        val categorias = catalogo
            .map { item -> item.subtitulo.trim() }
            .filter { it.isNotBlank() }
            .distinct()
            .sorted()

        val catalogoFiltrado = catalogo
            .filter { item ->
                coincide(filtros.consulta, item.titulo, item.subtitulo) &&
                    (filtros.categoria == null || item.subtitulo.trim() == filtros.categoria)
            }
            .ordenar(filtros.orden)

        val propiosFiltrados = propios
            .filter { producto ->
                coincide(filtros.consulta, producto.nombre, producto.categoria) &&
                    (filtros.categoria == null || producto.categoria.trim() == filtros.categoria)
            }
            .ordenarPropios(filtros.orden)

        ListaUiState(
            cargando = cargando,
            error = error,
            catalogo = catalogoFiltrado,
            propios = propiosFiltrados,
            categorias = categorias,
            consulta = filtros.consulta,
            categoria = filtros.categoria,
            orden = filtros.orden,
            totalCatalogo = catalogo.size,
            totalPropios = propios.size
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ListaUiState()
    )

    init {
        viewModelScope.launch {
            itemRepository.items
                .catch { error -> _error.value = mensajeDe(error) }
                .collect { items -> _catalogo.value = items }
        }

        viewModelScope.launch {
            productosRepository.observar()
                .catch { error ->
                    if (error is CancellationException) throw error
                    // Los productos propios son opcionales: si Firestore falla,
                    // el catalogo sigue funcionando.
                    _propios.value = emptyList()
                }
                .collect { productos -> _propios.value = productos }
        }

        cargar()
    }

    fun cargar() {
        viewModelScope.launch {
            _cargando.value = true
            _error.value = null
            itemRepository.refrescar()
                .onSuccess { cantidad ->
                    if (cantidad == 0 && _catalogo.value.isEmpty()) {
                        _error.value = "El catálogo está vacío. Revisa tu conexión."
                    }
                }
                .onFailure { fallo ->
                    if (_catalogo.value.isEmpty()) {
                        _error.value = mensajeDe(fallo)
                    } else {
                        _aviso.value = "No se pudo actualizar. Mostrando datos guardados."
                    }
                }
            _cargando.value = false
        }
    }

    fun buscar(texto: String) {
        _filtros.update { actual -> actual.copy(consulta = texto) }
    }

    fun filtrarPor(categoria: String?) {
        _filtros.update { actual ->
            actual.copy(categoria = if (actual.categoria == categoria) null else categoria)
        }
    }

    fun ordenar(orden: OrdenLista) {
        _filtros.update { actual -> actual.copy(orden = orden) }
    }

    fun alternarFavorito(id: Int) {
        viewModelScope.launch {
            itemRepository.alternarFavorito(id)
            val item = _catalogo.value.firstOrNull { it.id == id }
            if (item != null) {
                notificador.mostrar(
                    titulo = "Mi lista de compras",
                    texto = if (item.esFavorito) {
                        "${item.titulo} se quitó de tu lista"
                    } else {
                        "${item.titulo} se agregó a tu lista"
                    }
                )
            }
        }
    }

    fun avisoMostrado() {
        _aviso.value = null
    }

    private fun coincide(consulta: String, vararg campos: String): Boolean {
        if (consulta.isBlank()) return true
        val buscado = consulta.sinAcentos()
        return campos.any { campo -> campo.sinAcentos().contains(buscado) }
    }

    private fun List<Item>.ordenar(orden: OrdenLista): List<Item> = when (orden) {
        OrdenLista.NOMBRE -> sortedBy { it.titulo.lowercase(Locale.getDefault()) }
        OrdenLista.PRECIO_MENOR -> sortedBy { it.precio }
        OrdenLista.PRECIO_MAYOR -> sortedByDescending { it.precio }
    }

    private fun List<Producto>.ordenarPropios(orden: OrdenLista): List<Producto> = when (orden) {
        OrdenLista.NOMBRE -> sortedBy { it.nombre.lowercase(Locale.getDefault()) }
        OrdenLista.PRECIO_MENOR -> sortedBy { it.precio }
        OrdenLista.PRECIO_MAYOR -> sortedByDescending { it.precio }
    }

    private fun String.sinAcentos(): String =
        Normalizer.normalize(lowercase(Locale.getDefault()), Normalizer.Form.NFD)
            .replace(Regex("\\p{Mn}+"), "")

    private fun mensajeDe(error: Throwable): String = when (error) {
        is IOException -> "No hay conexión a internet. Revisa tu red e inténtalo de nuevo."
        is HttpException -> "El servidor respondió con un error (${error.code()})."
        else -> "No se pudo cargar el catálogo. Inténtalo de nuevo."
    }
}
