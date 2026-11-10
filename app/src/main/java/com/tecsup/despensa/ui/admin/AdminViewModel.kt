package com.tecsup.despensa.ui.admin

import androidx.lifecycle.ViewModel
import com.tecsup.despensa.ui.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AdminViewModel : ViewModel() {
    private val _estado = MutableStateFlow<UiState<Int>>(UiState.Cargando)
    val estado: StateFlow<UiState<Int>> = _estado.asStateFlow()
}
