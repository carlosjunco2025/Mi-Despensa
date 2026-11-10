package com.tecsup.despensa.data.auth

import com.tecsup.despensa.domain.model.Sesion

/**
 * Stub sin Firebase. La implementacion real de Firebase Auth la hara el companero.
 */
class FirebaseAuthRepository : AuthRepository {
    override suspend fun iniciarSesion(correo: String, clave: String): Result<Sesion> =
        Result.failure(UnsupportedOperationException("Pendiente: integrar Firebase Auth"))

    override suspend fun sesionActual(): Sesion? = null

    override fun cerrarSesion() = Unit
}
