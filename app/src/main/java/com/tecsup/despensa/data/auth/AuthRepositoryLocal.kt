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
        val nueva = crearSesion(correo, nombre = correo.substringBefore("@"))
        sesion = nueva
        return Result.success(nueva)
    }

    override suspend fun registrar(
        nombre: String,
        correo: String,
        clave: String
    ): Result<Sesion> {
        val nueva = crearSesion(correo, nombre)
        sesion = nueva
        return Result.success(nueva)
    }

    override suspend fun iniciarConGoogle(idToken: String): Result<Sesion> {
        val nueva = Sesion(
            uid = "local-google",
            correo = "invitado@gmail.com",
            rol = Rol.USUARIO,
            nombre = "Invitado"
        )
        sesion = nueva
        return Result.success(nueva)
    }

    override suspend fun sesionActual(): Sesion? = sesion

    override fun cerrarSesion() {
        sesion = null
    }

    private fun crearSesion(correo: String, nombre: String): Sesion {
        val limpio = correo.trim()
        val rol = if (limpio.lowercase().startsWith("admin")) Rol.ADMIN else Rol.USUARIO
        return Sesion(
            uid = "local-${limpio.hashCode()}",
            correo = limpio,
            rol = rol,
            nombre = nombre.trim().ifBlank { limpio.substringBefore("@") }
        )
    }
}
