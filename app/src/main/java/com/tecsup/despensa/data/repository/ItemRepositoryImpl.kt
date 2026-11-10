package com.tecsup.despensa.data.repository

import com.tecsup.despensa.data.remote.ApiService
import com.tecsup.despensa.data.remote.RetrofitClient
import com.tecsup.despensa.data.remote.dto.ItemDto
import com.tecsup.despensa.domain.model.Item
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine

/**
 * Implementacion en memoria. En H9 se reemplaza por Room como fuente unica de verdad.
 */
class ItemRepositoryImpl(
    private val api: ApiService = RetrofitClient.api
) : ItemRepository {

    private val _items = MutableStateFlow<List<Item>>(emptyList())
    private val _favoritos = MutableStateFlow<Set<Int>>(emptySet())

    override val items: Flow<List<Item>> = combine(_items, _favoritos) { items, ids ->
        items.map { item -> item.copy(esFavorito = item.id in ids) }
    }

    override val favoritos: Flow<List<Item>> = combine(_items, _favoritos) { items, ids ->
        items.filter { item -> item.id in ids }
            .map { item -> item.copy(esFavorito = true) }
    }

    override suspend fun refrescar(): Result<Int> = enResultado {
        val idsFavoritos = _favoritos.value
        val productos = api.listarProductos().products.map { dto ->
            dto.toItem(esFavorito = dto.id in idsFavoritos)
        }
        _items.value = productos
        productos.size
    }

    override suspend fun obtenerDetalle(id: Int): Result<Item> = enResultado {
        api.obtenerProducto(id).toItem(esFavorito = id in _favoritos.value)
    }

    override suspend fun alternarFavorito(id: Int) {
        _favoritos.value = _favoritos.value.let { actuales ->
            if (id in actuales) actuales - id else actuales + id
        }
    }

    override suspend fun cantidadGuardada(): Int = _items.value.size

    override suspend fun cantidadFavoritos(): Int = _favoritos.value.size

    override suspend fun limpiarCache() {
        _items.value = emptyList()
        _favoritos.value = emptySet()
    }

    private fun ItemDto.toItem(esFavorito: Boolean) = Item(
        id = id,
        titulo = title,
        subtitulo = brand ?: category,
        imagenUrl = thumbnail ?: images.firstOrNull(),
        precio = price,
        esFavorito = esFavorito
    )

    private inline fun <T> enResultado(bloque: () -> T): Result<T> = try {
        Result.success(bloque())
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        Result.failure(e)
    }
}
