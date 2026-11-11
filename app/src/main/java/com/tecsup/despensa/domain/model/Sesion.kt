package com.tecsup.despensa.domain.model

enum class Rol {
    ADMIN,
    USUARIO
}

data class Sesion(
    val uid: String,
    val correo: String,
    val rol: Rol,
    val nombre: String = ""
) {
    val inicial: String
        get() = nombre.trim().firstOrNull()?.uppercase()
            ?: correo.trim().firstOrNull()?.uppercase()?.toString()
            ?: "D"

    val esAdmin: Boolean
        get() = rol == Rol.ADMIN
}
