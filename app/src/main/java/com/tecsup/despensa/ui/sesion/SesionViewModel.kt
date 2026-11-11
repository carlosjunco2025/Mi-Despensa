package com.tecsup.despensa.ui.sesion

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tecsup.despensa.data.auth.AuthRepository
import com.tecsup.despensa.domain.model.Sesion
import com.tecsup.despensa.ui.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Mantiene la sesion actual de la app. Al iniciar consulta sesionActual()
 * para decidir si se abre el login o directamente el catalogo.
 */
class SesionViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _estado = MutableStateFlow<UiState<Sesion?>>(UiState.Cargando)
    val estado: StateFlow<UiState<Sesion?>> = _estado.asStateFlow()

    init {
        cargarSesion()
    }

    fun cargarSesion() {
        viewModelScope.launch {
            _estado.value = UiState.Cargando
            _estado.value = UiState.Exito(authRepository.sesionActual())
        }
    }

    fun establecerSesion(sesion: Sesion) {
        _estado.value = UiState.Exito(sesion)
    }

    fun cerrarSesion() {
        authRepository.cerrarSesion()
        _estado.value = UiState.Exito(null)
    }
}
