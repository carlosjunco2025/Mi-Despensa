package com.tecsup.despensa.data.repository

import com.tecsup.despensa.domain.model.Item
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

/**
 * Stub inicial. En H6 se conecta a la API y en H9 pasa a usar Room como fuente unica.
 */
class ItemRepositoryImpl : ItemRepository {
    override val items: Flow<List<Item>> = flowOf(emptyList())
    override val favoritos: Flow<List<Item>> = flowOf(emptyList())

    override suspend fun refrescar(): Result<Int> = Result.success(0)

    override suspend fun obtenerDetalle(id: Int): Result<Item> =
        Result.failure(UnsupportedOperationException("Pendiente: obtener detalle"))

    override suspend fun alternarFavorito(id: Int) = Unit

    override suspend fun cantidadGuardada(): Int = 0

    override suspend fun cantidadFavoritos(): Int = 0

    override suspend fun limpiarCache() = Unit
}
