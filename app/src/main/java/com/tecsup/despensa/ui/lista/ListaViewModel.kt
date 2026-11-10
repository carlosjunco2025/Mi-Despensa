package com.tecsup.despensa.ui.lista

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tecsup.despensa.data.repository.ItemRepository
import com.tecsup.despensa.domain.model.Item
import com.tecsup.despensa.notification.Notificador
import com.tecsup.despensa.ui.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

class ListaViewModel(
    private val itemRepository: ItemRepository,
    private val notificador: Notificador
) : ViewModel() {

    private val _estado = MutableStateFlow<UiState<List<Item>>>(UiState.Cargando)
    val estado: StateFlow<UiState<List<Item>>> = _estado.asStateFlow()

    private val _aviso = MutableStateFlow<String?>(null)
    val aviso: StateFlow<String?> = _aviso.asStateFlow()

    private var cargaCompletada = false

    init {
        observarItems()
        cargar()
    }

    fun cargar() {
        viewModelScope.launch {
            _estado.value = UiState.Cargando
            _aviso.value = null
            itemRepository.refrescar()
                .onSuccess { cantidad ->
                    cargaCompletada = true
                    notificador.mostrar(
                        "Datos actualizados",
                        "Se guardaron $cantidad elementos"
                    )
                    _estado.value = UiState.Exito(itemRepository.items.first())
                }
                .onFailure { error ->
                    val guardados = itemRepository.cantidadGuardada()
                    if (error is IOException && guardados > 0) {
                        cargaCompletada = true
                        _aviso.value = "Sin conexión: mostrando datos guardados"
                        _estado.value = UiState.Exito(itemRepository.items.first())
                    } else {
                        _estado.value = UiState.Error(mensajeDe(error))
                    }
                }
        }
    }

    private fun observarItems() {
        viewModelScope.launch {
            itemRepository.items.collect { items ->
                if (cargaCompletada) {
                    _estado.value = UiState.Exito(items)
                }
            }
        }
    }

    private fun mensajeDe(error: Throwable): String = when (error) {
        is IOException -> "No hay conexión a internet"
        is HttpException -> "El servidor respondió con error ${error.code()}"
        else -> "Ocurrió un error inesperado"
    }
}
