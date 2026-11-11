package com.tecsup.despensa.domain.model

enum class Rol {
    ADMIN,
    USUARIO
}

data class Sesion(
    val uid: String,
    val correo: String,
    val nombre: String = "",
    val rol: Rol
)
