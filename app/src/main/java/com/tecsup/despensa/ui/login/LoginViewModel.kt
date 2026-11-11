package com.tecsup.despensa.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.firestore.FirebaseFirestoreException
import com.tecsup.despensa.data.auth.AuthRepository
import com.tecsup.despensa.domain.model.Sesion
import com.tecsup.despensa.ui.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.IOException

/**
 * Logica de la pantalla de acceso: inicio de sesion, registro de cuenta nueva
 * y acceso con Google. Toda la validacion vive aqui para que la pantalla solo
 * se encargue de dibujar.
 */
class LoginViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _estado = MutableStateFlow<UiState<Sesion?>>(UiState.Exito(null))
    val estado: StateFlow<UiState<Sesion?>> = _estado.asStateFlow()

    fun iniciarSesion(correo: String, clave: String) {
        val error = validarCorreoClave(correo, clave)
        if (error != null) {
            _estado.value = UiState.Error(error)
            return
        }

        viewModelScope.launch {
            _estado.value = UiState.Cargando
            authRepository.iniciarSesion(correo.trim(), clave)
                .onSuccess { sesion -> _estado.value = UiState.Exito(sesion) }
                .onFailure { fallo -> _estado.value = UiState.Error(mensajeDe(fallo)) }
        }
    }

    fun registrar(
        nombre: String,
        correo: String,
        clave: String,
        confirmacion: String
    ) {
        val error = when {
            nombre.isBlank() -> "Escribe tu nombre"
            clave != confirmacion -> "Las contraseñas no coinciden"
            else -> validarCorreoClave(correo, clave)
        }
        if (error != null) {
            _estado.value = UiState.Error(error)
            return
        }

        viewModelScope.launch {
            _estado.value = UiState.Cargando
            authRepository.registrar(nombre.trim(), correo.trim(), clave)
                .onSuccess { sesion -> _estado.value = UiState.Exito(sesion) }
                .onFailure { fallo -> _estado.value = UiState.Error(mensajeDe(fallo)) }
        }
    }

    fun iniciarConGoogle(idToken: String) {
        viewModelScope.launch {
            _estado.value = UiState.Cargando
            authRepository.iniciarConGoogle(idToken)
                .onSuccess { sesion -> _estado.value = UiState.Exito(sesion) }
                .onFailure { fallo -> _estado.value = UiState.Error(mensajeDe(fallo)) }
        }
    }

    /** Notifica un error que ocurre antes de llamar al repositorio (p. ej. Google cancelado). */
    fun reportarError(mensaje: String) {
        _estado.value = UiState.Error(mensaje)
    }

    fun limpiarError() {
        if (_estado.value is UiState.Error) _estado.value = UiState.Exito(null)
    }

    fun sesionConsumida() {
        _estado.value = UiState.Exito(null)
    }

    private fun validarCorreoClave(correo: String, clave: String): String? = when {
        correo.isBlank() -> "Ingresa tu correo"
        !correo.contains("@") || !correo.contains(".") -> "El correo no tiene un formato válido"
        clave.isBlank() -> "Ingresa tu contraseña"
        clave.length < 6 -> "La contraseña debe tener al menos 6 caracteres"
        else -> null
    }

    private fun mensajeDe(error: Throwable): String = when (error) {
        is FirebaseAuthWeakPasswordException ->
            "La contraseña es muy débil: usa al menos 6 caracteres"

        is FirebaseAuthUserCollisionException ->
            "Ya existe una cuenta con ese correo"

        is FirebaseAuthInvalidCredentialsException -> when (error.errorCode) {
            "ERROR_INVALID_EMAIL" -> "El correo no tiene un formato válido"
            "ERROR_WRONG_PASSWORD" -> "La contraseña es incorrecta"
            else -> "Correo o contraseña incorrectos"
        }

        is FirebaseAuthException -> when (error.errorCode) {
            "ERROR_INVALID_EMAIL" -> "El correo no tiene un formato válido"
            "ERROR_WRONG_PASSWORD",
            "ERROR_INVALID_CREDENTIAL",
            "ERROR_USER_NOT_FOUND" -> "Correo o contraseña incorrectos"
            "ERROR_EMAIL_ALREADY_IN_USE" -> "Ya existe una cuenta con ese correo"
            "ERROR_WEAK_PASSWORD" -> "La contraseña debe tener al menos 6 caracteres"
            "ERROR_NETWORK_REQUEST_FAILED" -> "No hay conexión a internet"
            "ERROR_TOO_MANY_REQUESTS" -> "Demasiados intentos. Espera un momento"
            "ERROR_OPERATION_NOT_ALLOWED" ->
                "El acceso con correo está deshabilitado en Firebase"
            "ERROR_ACCOUNT_EXISTS_WITH_DIFFERENT_CREDENTIAL" ->
                "Ese correo ya está registrado con otro método de acceso"
            else -> "No se pudo completar el acceso. Inténtalo de nuevo"
        }

        is FirebaseFirestoreException -> when (error.code) {
            FirebaseFirestoreException.Code.PERMISSION_DENIED ->
                "No se pudo leer tu perfil. Revisa las reglas de Firestore"
            else -> "No se pudo verificar tu cuenta"
        }

        is IOException -> "No hay conexión a internet"
        else -> error.message?.takeIf { it.isNotBlank() }
            ?: "No se pudo completar el acceso. Inténtalo de nuevo"
    }
}
