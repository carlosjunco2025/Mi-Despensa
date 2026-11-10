package com.tecsup.despensa.ui.lista

import androidx.lifecycle.ViewModel
import com.tecsup.despensa.domain.model.Item
import com.tecsup.despensa.ui.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ListaViewModel : ViewModel() {
    private val _estado = MutableStateFlow<UiState<List<Item>>>(UiState.Cargando)
    val estado: StateFlow<UiState<List<Item>>> = _estado.asStateFlow()
}
