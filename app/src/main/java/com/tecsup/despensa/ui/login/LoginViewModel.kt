package com.tecsup.despensa.ui.login

import androidx.lifecycle.ViewModel
import com.tecsup.despensa.domain.model.Sesion
import com.tecsup.despensa.ui.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class LoginViewModel : ViewModel() {
    private val _estado = MutableStateFlow<UiState<Sesion>>(UiState.Cargando)
    val estado: StateFlow<UiState<Sesion>> = _estado.asStateFlow()
}
