package com.tecsup.despensa.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Representacion persistente de un producto de la despensa.
 * esFavorito se conserva al sincronizar con la API.
 */
@Entity(tableName = "items")
data class ItemEntity(
    @PrimaryKey val id: Int,
    val titulo: String,
    val subtitulo: String,
    val imagenUrl: String?,
    val precio: Double,
    val esFavorito: Boolean = false
)
