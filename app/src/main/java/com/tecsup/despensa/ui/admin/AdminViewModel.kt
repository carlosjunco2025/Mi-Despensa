package com.tecsup.despensa.ui.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tecsup.despensa.data.repository.ItemRepository
import com.tecsup.despensa.notification.Notificador
import com.tecsup.despensa.ui.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

data class ResumenAdmin(
    val totalGuardado: Int,
    val totalFavoritos: Int
)

/**
 * Panel de administracion. Los totales salen de Room, por eso se actualizan
 * solos al sincronizar o al limpiar el cache. La notificacion se pide a traves
 * de la interface Notificador, nunca desde la pantalla.
 */
class AdminViewModel(
    private val itemRepository: ItemRepository,
    private val notificador: Notificador
) : ViewModel() {

    private val _estado = MutableStateFlow<UiState<ResumenAdmin>>(UiState.Cargando)
    val estado: StateFlow<UiState<ResumenAdmin>> = _estado.asStateFlow()

    private val _sincronizando = MutableStateFlow(false)
    val sincronizando: StateFlow<Boolean> = _sincronizando.asStateFlow()

    private val _mensaje = MutableStateFlow<String?>(null)
    val mensaje: StateFlow<String?> = _mensaje.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                itemRepository.items,
                itemRepository.favoritos
            ) { items, favoritos ->
                ResumenAdmin(
                    totalGuardado = items.size,
                    totalFavoritos = favoritos.size
                )
            }.collect { resumen ->
                _estado.value = UiState.Exito(resumen)
            }
        }
    }

    fun sincronizar() {
        if (_sincronizando.value) return

        viewModelScope.launch {
            _sincronizando.value = true
            _mensaje.value = null

            itemRepository.refrescar()
                .onSuccess { cantidad ->
                    notificador.mostrar(
                        "Datos actualizados",
                        "Se guardaron $cantidad elementos"
                    )
                    _mensaje.value = "Se guardaron $cantidad elementos"
                }
                .onFailure { error ->
                    _mensaje.value = mensajeDe(error)
                }

            _sincronizando.value = false
        }
    }

    fun limpiarCache() {
        viewModelScope.launch {
            itemRepository.limpiarCache()
            _mensaje.value = "Caché eliminada: la lista quedó vacía"
        }
    }

    fun consumirMensaje() {
        _mensaje.value = null
    }

    private fun mensajeDe(error: Throwable): String = when (error) {
        is IOException -> "No hay conexión a internet"
        is HttpException -> "El servidor respondió con error ${error.code()}"
        else -> "No se pudo sincronizar"
    }
}
