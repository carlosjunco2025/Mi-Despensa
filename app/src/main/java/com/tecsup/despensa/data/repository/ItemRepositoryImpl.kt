package com.tecsup.despensa.data.repository

import com.tecsup.despensa.data.local.ItemDao
import com.tecsup.despensa.data.mapper.toDomain
import com.tecsup.despensa.data.mapper.toEntity
import com.tecsup.despensa.data.remote.ApiService
import com.tecsup.despensa.data.remote.RetrofitClient
import com.tecsup.despensa.domain.model.Item
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Room es la fuente unica de verdad: la UI observa la base de datos y
 * la API solo se usa para sincronizar y actualizar ese cache local.
 */
class ItemRepositoryImpl(
    private val api: ApiService = RetrofitClient.api,
    private val dao: ItemDao
) : ItemRepository {

    override val items: Flow<List<Item>> = dao.observarItems()
        .map { entidades -> entidades.map { entidad -> entidad.toDomain() } }

    override val favoritos: Flow<List<Item>> = dao.observarFavoritos()
        .map { entidades -> entidades.map { entidad -> entidad.toDomain() } }

    override suspend fun refrescar(): Result<Int> = enResultado {
        val productos = api.listarProductos().products.map { dto -> dto.toEntity() }
        dao.upsert(productos)
        productos.size
    }

    override suspend fun obtenerDetalle(id: Int): Result<Item> = enResultado {
        val favorito = dao.obtenerPorId(id)?.esFavorito ?: false
        val entidad = api.obtenerProducto(id).toEntity(esFavorito = favorito)
        dao.upsert(entidad)
        entidad.toDomain()
    }

    override suspend fun alternarFavorito(id: Int) {
        dao.alternarFavorito(id)
    }

    override suspend fun cantidadGuardada(): Int = dao.contar()

    override suspend fun cantidadFavoritos(): Int = dao.contarFavoritos()

    override suspend fun limpiarCache() {
        dao.limpiar()
    }

    private inline fun <T> enResultado(bloque: () -> T): Result<T> = try {
        Result.success(bloque())
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        Result.failure(e)
    }
}
