package com.tecsup.despensa.data.mapper

import com.tecsup.despensa.data.local.ItemEntity
import com.tecsup.despensa.data.remote.dto.ItemDto
import com.tecsup.despensa.domain.model.Item

fun ItemDto.toEntity(esFavorito: Boolean = false) = ItemEntity(
    id = id,
    titulo = title,
    subtitulo = brand ?: category,
    imagenUrl = thumbnail ?: images.firstOrNull(),
    precio = price,
    esFavorito = esFavorito
)

fun ItemEntity.toDomain() = Item(
    id = id,
    titulo = titulo,
    subtitulo = subtitulo,
    imagenUrl = imagenUrl,
    precio = precio,
    esFavorito = esFavorito
)
