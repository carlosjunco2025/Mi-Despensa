package com.tecsup.despensa.ui.favoritos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tecsup.despensa.data.repository.ItemRepository
import com.tecsup.despensa.domain.model.Item
import com.tecsup.despensa.ui.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Expone los favoritos guardados en Room. Como Room es la fuente unica
 * de verdad, la lista se actualiza sola al marcar o desmarcar productos.
 */
class FavoritosViewModel(
    private val itemRepository: ItemRepository
) : ViewModel() {

    private val _estado = MutableStateFlow<UiState<List<Item>>>(UiState.Cargando)
    val estado: StateFlow<UiState<List<Item>>> = _estado.asStateFlow()

    init {
        viewModelScope.launch {
            itemRepository.favoritos.collect { items ->
                _estado.value = UiState.Exito(items)
            }
        }
    }

    fun quitar(id: Int) {
        viewModelScope.launch {
            itemRepository.alternarFavorito(id)
        }
    }
}
