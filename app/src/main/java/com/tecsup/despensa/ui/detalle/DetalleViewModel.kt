package com.tecsup.despensa.ui.detalle

import androidx.lifecycle.ViewModel
import com.tecsup.despensa.domain.model.Item
import com.tecsup.despensa.ui.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class DetalleViewModel : ViewModel() {
    private val _estado = MutableStateFlow<UiState<Item>>(UiState.Cargando)
    val estado: StateFlow<UiState<Item>> = _estado.asStateFlow()
}
