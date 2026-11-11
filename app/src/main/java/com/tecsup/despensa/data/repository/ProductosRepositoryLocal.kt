package com.tecsup.despensa.data.repository

import com.tecsup.despensa.domain.model.Producto
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Version en memoria de ProductosRepository para cuando no hay Firebase
 * configurado. Arranca con tres productos de ejemplo y permite crear,
 * editar y eliminar durante la sesion.
 */
class ProductosRepositoryLocal : ProductosRepository {

    private val _productos = MutableStateFlow(productosIniciales())

    override fun observar(): Flow<List<Producto>> = _productos.asStateFlow()

    override suspend fun crear(producto: Producto): Result<Unit> {
        val nuevo = producto.copy(id = "local-${System.currentTimeMillis()}")
        _productos.update { actuales -> (actuales + nuevo).sortedBy { it.nombre } }
        return Result.success(Unit)
    }

    override suspend fun actualizar(producto: Producto): Result<Unit> {
        _productos.update { actuales ->
            actuales.map { existente -> if (existente.id == producto.id) producto else existente }
                .sortedBy { it.nombre }
        }
        return Result.success(Unit)
    }

    override suspend fun eliminar(id: String): Result<Unit> {
        _productos.update { actuales -> actuales.filterNot { it.id == id } }
        return Result.success(Unit)
    }

    private fun productosIniciales(): List<Producto> = listOf(
        Producto(
            id = "local-1",
            nombre = "Manzanas rojas",
            categoria = "Frutas",
            precio = 6.90,
            stock = 12,
            emoji = "🍎",
            descripcion = "Manzanas frescas del valle, ideal para el desayuno."
        ),
        Producto(
            id = "local-2",
            nombre = "Leche entera",
            categoria = "Lácteos",
            precio = 4.50,
            stock = 8,
            emoji = "🥛",
            descripcion = "Leche entera de un litro, larga vida."
        ),
        Producto(
            id = "local-3",
            nombre = "Pan integral",
            categoria = "Panadería",
            precio = 5.20,
            stock = 5,
            emoji = "🍞",
            descripcion = "Pan integral recién horneado, rico en fibra."
        )
    )
}
