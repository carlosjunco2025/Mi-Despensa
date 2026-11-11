package com.tecsup.despensa.data.auth

import com.tecsup.despensa.domain.model.Rol
import com.tecsup.despensa.domain.model.Sesion

/**
 * Autenticacion local de respaldo.
 *
 * Se usa solo cuando el proyecto no tiene google-services.json, para que la app
 * compile y se pueda navegar sin Firebase. Acepta cualquier correo y asigna el
 * rol ADMIN a los correos que empiezan con "admin".
 */
class AuthRepositoryLocal : AuthRepository {

    private var sesion: Sesion? = null

    override suspend fun iniciarSesion(correo: String, clave: String): Result<Sesion> {
        val rol = if (correo.trim().lowercase().startsWith("admin")) Rol.ADMIN else Rol.USUARIO
        val nueva = Sesion(uid = "local-uid", correo = correo.trim(), rol = rol)
        sesion = nueva
        return Result.success(nueva)
    }

    override suspend fun sesionActual(): Sesion? = sesion

    override fun cerrarSesion() {
        sesion = null
    }
}
