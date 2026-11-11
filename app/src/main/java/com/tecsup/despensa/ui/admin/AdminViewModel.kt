package com.tecsup.despensa.ui.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tecsup.despensa.data.repository.ItemRepository
import com.tecsup.despensa.data.repository.ProductosRepository
import com.tecsup.despensa.domain.model.Producto
import com.tecsup.despensa.notification.Notificador
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

data class ResumenAdmin(
    val totalCatalogo: Int = 0,
    val totalFavoritos: Int = 0,
    val totalPropios: Int = 0,
    val valorInventario: Double = 0.0
)

data class AdminUiState(
    val cargando: Boolean = true,
    val guardando: Boolean = false,
    val error: String? = null,
    val resumen: ResumenAdmin = ResumenAdmin(),
    val productos: List<Producto> = emptyList()
)

/**
 * Panel de administracion: estadisticas de la despensa, CRUD de productos
 * propios (Firestore) y mantenimiento del catalogo local (Room).
 */
class AdminViewModel(
    private val itemRepository: ItemRepository,
    private val productosRepository: ProductosRepository,
    private val notificador: Notificador
) : ViewModel() {

    private val _estado = MutableStateFlow(AdminUiState())
    val estado: StateFlow<AdminUiState> = _estado.asStateFlow()

    private val _aviso = MutableStateFlow<String?>(null)
    val aviso: StateFlow<String?> = _aviso.asStateFlow()

    init {
        viewModelScope.launch {
            productosRepository.observar()
                .catch { error ->
                    if (error is CancellationException) throw error
                    _estado.update {
                        it.copy(
                            error = "No se pudieron leer los productos propios. " +
                                "Revisa las reglas de Firestore.",
                            cargando = false
                        )
                    }
                }
                .collect { productos ->
                    _estado.update { actual ->
                        actual.copy(
                            productos = productos,
                            cargando = false,
                            resumen = actual.resumen.copy(
                                totalPropios = productos.size,
                                valorInventario = productos.sumOf { producto ->
                                    producto.precio * producto.stock
                                }
                            )
                        )
                    }
                }
        }

        actualizarTotales()
        cargarCatalogo()
    }

    fun actualizarTotales() {
        viewModelScope.launch {
            val guardados = itemRepository.cantidadGuardada()
            val favoritos = itemRepository.cantidadFavoritos()
            _estado.update { actual ->
                actual.copy(
                    resumen = actual.resumen.copy(
                        totalCatalogo = guardados,
                        totalFavoritos = favoritos
                    )
                )
            }
        }
    }

    fun sincronizar() {
        viewModelScope.launch {
            _estado.update { it.copy(cargando = true, error = null) }
            itemRepository.refrescar()
                .onSuccess { cantidad ->
                    _aviso.value = "Catálogo actualizado: $cantidad productos"
                    notificador.mostrar(
                        titulo = "Despensa actualizada",
                        texto = "Se sincronizaron $cantidad productos del catálogo"
                    )
                }
                .onFailure { fallo ->
                    _estado.update { it.copy(error = mensajeDe(fallo)) }
                }
            actualizarTotales()
            _estado.update { it.copy(cargando = false) }
        }
    }

    private fun cargarCatalogo() {
        viewModelScope.launch {
            itemRepository.refrescar()
                .onFailure { fallo ->
                    if (itemRepository.cantidadGuardada() == 0) {
                        _estado.update { it.copy(error = mensajeDe(fallo)) }
                    }
                }
            actualizarTotales()
            _estado.update { it.copy(cargando = false) }
        }
    }

    fun limpiarCache() {
        viewModelScope.launch {
            itemRepository.limpiarCache()
            _estado.update {
                it.copy(
                    resumen = it.resumen.copy(totalCatalogo = 0, totalFavoritos = 0)
                )
            }
            _aviso.value = "Caché local limpiada. Sincroniza para volver a cargar."
        }
    }

    /**
     * Crea el producto si no tiene id y lo actualiza si ya existe.
     * Devuelve true cuando se pudo guardar, para que la pantalla limpie el formulario.
     */
    fun guardar(producto: Producto, alGuardar: () -> Unit = {}) {
        val error = validar(producto)
        if (error != null) {
            _aviso.value = error
            return
        }

        viewModelScope.launch {
            _estado.update { it.copy(guardando = true) }
            val limpio = producto.copy(
                nombre = producto.nombre.trim(),
                categoria = producto.categoria.trim().ifBlank { "General" },
                descripcion = producto.descripcion.trim()
            )
            val resultado = if (limpio.id.isBlank()) {
                productosRepository.crear(limpio)
            } else {
                productosRepository.actualizar(limpio)
            }

            resultado
                .onSuccess {
                    _aviso.value = if (limpio.id.isBlank()) {
                        "\"${limpio.nombre}\" se agregó a tu despensa"
                    } else {
                        "\"${limpio.nombre}\" se actualizó correctamente"
                    }
                    notificador.mostrar(
                        titulo = "Producto guardado",
                        texto = "${limpio.nombre} ya está en tu despensa"
                    )
                    alGuardar()
                }
                .onFailure { fallo -> _aviso.value = mensajeDe(fallo) }

            _estado.update { it.copy(guardando = false) }
            actualizarTotales()
        }
    }

    fun eliminar(producto: Producto) {
        viewModelScope.launch {
            productosRepository.eliminar(producto.id)
                .onSuccess {
                    _aviso.value = "\"${producto.nombre}\" se eliminó de tu despensa"
                    notificador.mostrar(
                        titulo = "Producto eliminado",
                        texto = "${producto.nombre} ya no está en tu despensa"
                    )
                }
                .onFailure { fallo -> _aviso.value = mensajeDe(fallo) }
            actualizarTotales()
        }
    }

    fun avisoMostrado() {
        _aviso.value = null
    }

    private fun validar(producto: Producto): String? = when {
        producto.nombre.isBlank() -> "Escribe el nombre del producto"
        producto.precio < 0 -> "El precio no puede ser negativo"
        producto.stock < 0 -> "El stock no puede ser negativo"
        else -> null
    }

    private fun mensajeDe(error: Throwable): String = when (error) {
        is IOException -> "No hay conexión a internet. Inténtalo de nuevo."
        is HttpException -> "El servidor respondió con un error (${error.code()})."
        else -> {
            val texto = error.message.orEmpty()
            if (texto.contains("PERMISSION_DENIED", ignoreCase = true)) {
                "Firestore rechazó la operación. Revisa las reglas de la colección productos."
            } else {
                "No se pudo completar la operación. Inténtalo de nuevo."
            }
        }
    }
}
