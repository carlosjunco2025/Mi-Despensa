package com.tecsup.despensa.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.firestore.FirebaseFirestoreException
import com.tecsup.despensa.data.auth.AuthRepository
import com.tecsup.despensa.data.repository.ItemRepository
import com.tecsup.despensa.domain.model.Sesion
import com.tecsup.despensa.ui.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.IOException

class LoginViewModel(
    private val authRepository: AuthRepository,
    private val itemRepository: ItemRepository
) : ViewModel() {

    private val _estado = MutableStateFlow<UiState<Sesion?>>(UiState.Exito(null))
    val estado: StateFlow<UiState<Sesion?>> = _estado.asStateFlow()

    fun iniciarSesion(correo: String, clave: String) {
        if (correo.isBlank() || clave.isBlank()) {
            _estado.value = UiState.Error("Ingresa tu correo y contraseña")
            return
        }

        viewModelScope.launch {
            _estado.value = UiState.Cargando
            authRepository.iniciarSesion(correo.trim(), clave)
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
            "ERROR_INVALID_EMAIL" ->
                "El correo no tiene un formato válido"

            "ERROR_WRONG_PASSWORD",
            "ERROR_INVALID_CREDENTIAL",
            "ERROR_USER_NOT_FOUND" ->
                "Correo o contraseña incorrectos"

            "ERROR_NETWORK_REQUEST_FAILED" ->
                "No hay conexión a internet"

            "ERROR_TOO_MANY_REQUESTS" ->
                "Demasiados intentos. Espera un momento"

            else -> "No se pudo iniciar sesión. Inténtalo de nuevo"
        }

        is FirebaseFirestoreException -> when (error.code) {
            FirebaseFirestoreException.Code.PERMISSION_DENIED ->
                "No se pudo leer tu rol. Revisa los permisos de Firestore"

            else -> "No se pudo verificar tu rol"
        }

        is IOException -> "No hay conexión a internet"
        else -> "No se pudo iniciar sesión. Inténtalo de nuevo"
    }
}
