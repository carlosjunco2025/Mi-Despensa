package com.tecsup.despensa.data.repository

import com.tecsup.despensa.domain.model.Item
import kotlinx.coroutines.flow.Flow

interface ItemRepository {
    val items: Flow<List<Item>>
    val favoritos: Flow<List<Item>>

    suspend fun refrescar(): Result<Int>

    suspend fun obtenerDetalle(id: Int): Result<Item>

    suspend fun alternarFavorito(id: Int)

    suspend fun cantidadGuardada(): Int

    suspend fun cantidadFavoritos(): Int

    suspend fun limpiarCache()
}
