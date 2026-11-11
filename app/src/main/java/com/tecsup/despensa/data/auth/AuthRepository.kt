package com.tecsup.despensa.data.auth

import com.tecsup.despensa.domain.model.Sesion

interface AuthRepository {
    suspend fun iniciarSesion(correo: String, clave: String): Result<Sesion>

    suspend fun registrarUsuario(nombre: String, correo: String, clave: String): Result<Sesion>

    suspend fun sesionActual(): Sesion?

    fun cerrarSesion()
}
