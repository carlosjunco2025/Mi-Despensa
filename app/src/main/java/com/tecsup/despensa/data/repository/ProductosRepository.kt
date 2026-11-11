package com.tecsup.despensa.data.repository

import com.tecsup.despensa.domain.model.Producto
import kotlinx.coroutines.flow.Flow

/**
 * Productos propios de la despensa (los que crea el administrador).
 * La implementacion real usa Firestore; sin google-services.json se usa una
 * implementacion local en memoria para poder demostrar la app.
 */
interface ProductosRepository {
    fun observar(): Flow<List<Producto>>

    suspend fun crear(producto: Producto): Result<Unit>

    suspend fun actualizar(producto: Producto): Result<Unit>

    suspend fun eliminar(id: String): Result<Unit>
}
