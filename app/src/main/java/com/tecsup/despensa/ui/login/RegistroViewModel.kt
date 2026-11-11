package com.tecsup.despensa.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuthException
import com.tecsup.despensa.data.auth.AuthRepository
import com.tecsup.despensa.data.repository.ItemRepository
import com.tecsup.despensa.domain.model.Sesion
import com.tecsup.despensa.ui.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.IOException

class RegistroViewModel(
    private val authRepository: AuthRepository,
    private val itemRepository: ItemRepository
) : ViewModel() {

    private val _estado = MutableStateFlow<UiState<Sesion?>>(UiState.Exito(null))
    val estado: StateFlow<UiState<Sesion?>> = _estado.asStateFlow()

    fun registrar(nombre: String, correo: String, clave: String, confirmarClave: String) {
        if (nombre.isBlank() || correo.isBlank() || clave.isBlank() || confirmarClave.isBlank()) {
            _estado.value = UiState.Error("Completa todos los campos")
            return
        }

        if (clave != confirmarClave) {
            _estado.value = UiState.Error("Las contraseñas no coinciden")
            return
        }

        if (clave.length < 6) {
            _estado.value = UiState.Error("La contraseña debe tener al menos 6 caracteres")
            return
        }

        viewModelScope.launch {
            _estado.value = UiState.Cargando
            authRepository.registrarUsuario(nombre.trim(), correo.trim(), clave)
                .onSuccess { sesion ->
                    itemRepository.limpiarCache()
                    _estado.value = UiState.Exito(sesion)
                }
                .onFailure { error -> _estado.value = UiState.Error(mensajeDe(error)) }
        }
    }

    fun sesionConsumida() {
        _estado.value = UiState.Exito(null)
    }

    private fun mensajeDe(error: Throwable): String = when (error) {
        is FirebaseAuthException -> when (error.errorCode) {
            "ERROR_EMAIL_ALREADY_IN_USE" -> "El correo ya está registrado"
            "ERROR_INVALID_EMAIL" -> "El correo no tiene un formato válido"
            "ERROR_WEAK_PASSWORD" -> "La contraseña es muy débil"
            "ERROR_NETWORK_REQUEST_FAILED" -> "No hay conexión a internet"
            else -> "No se pudo registrar el usuario"
        }
        is IOException -> "No hay conexión a internet"
        else -> error.localizedMessage ?: "No se pudo registrar el usuario"
    }
}
