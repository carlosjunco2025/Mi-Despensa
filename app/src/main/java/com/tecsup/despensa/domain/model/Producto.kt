package com.tecsup.despensa.domain.model

/**
 * Producto propio de la despensa, creado por un administrador y guardado en
 * Firestore (coleccion "productos"). Se diferencia del catalogo de DummyJSON,
 * que se guarda en Room como cache.
 */
data class Producto(
    val id: String = "",
    val nombre: String = "",
    val categoria: String = "General",
    val precio: Double = 0.0,
    val stock: Int = 0,
    val emoji: String = "🧺",
    val descripcion: String = "",
    val creadoPor: String = ""
)
