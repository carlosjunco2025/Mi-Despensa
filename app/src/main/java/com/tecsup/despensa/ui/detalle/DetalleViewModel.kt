package com.tecsup.despensa.ui.detalle

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tecsup.despensa.data.repository.ItemRepository
import com.tecsup.despensa.domain.model.Item
import com.tecsup.despensa.ui.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

/**
 * Carga el detalle de un producto con el segundo endpoint del repositorio.
 * Guarda el id cargado para no volver a descargar al girar la pantalla.
 */
class DetalleViewModel(
    private val itemRepository: ItemRepository
) : ViewModel() {

    private val _estado = MutableStateFlow<UiState<Item>>(UiState.Cargando)
    val estado: StateFlow<UiState<Item>> = _estado.asStateFlow()

    private var idCargado: Int? = null

    fun cargar(id: Int) {
        if (idCargado == id && _estado.value !is UiState.Error) return
        idCargado = id

        viewModelScope.launch {
            _estado.value = UiState.Cargando
            itemRepository.obtenerDetalle(id)
                .onSuccess { item -> _estado.value = UiState.Exito(item) }
                .onFailure { error -> _estado.value = UiState.Error(mensajeDe(error)) }
        }
    }

    fun reintentar() {
        val id = idCargado ?: return
        idCargado = null
        cargar(id)
    }

    fun alternarFavorito() {
        val actual = (_estado.value as? UiState.Exito)?.datos ?: return
        viewModelScope.launch {
            itemRepository.alternarFavorito(actual.id)
            _estado.value = UiState.Exito(actual.copy(esFavorito = !actual.esFavorito))
        }
    }

    private fun mensajeDe(error: Throwable): String = when (error) {
        is IOException -> "No hay conexión a internet"
        is HttpException -> "El servidor respondió con error ${error.code()}"
        else -> "No se pudo cargar el producto"
    }
}
