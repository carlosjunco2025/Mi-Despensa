package com.tecsup.despensa.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.tecsup.despensa.domain.model.Producto
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/**
 * CRUD de productos propios sobre la coleccion "productos" de Firestore.
 * observar() mantiene la lista sincronizada en tiempo real con la nube.
 */
class ProductosRepositoryFirestore(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) : ProductosRepository {

    private val coleccion get() = firestore.collection("productos")

    override fun observar(): Flow<List<Producto>> = callbackFlow {
        val registro = coleccion
            .orderBy("nombre", Query.Direction.ASCENDING)
            .addSnapshotListener { instantanea, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val productos = instantanea?.documents
                    ?.mapNotNull { documento -> documento.aProducto() }
                    ?: emptyList()
                trySend(productos)
            }
        awaitClose { registro.remove() }
    }

    override suspend fun crear(producto: Producto): Result<Unit> = enResultado {
        val autor = auth.currentUser?.uid ?: producto.creadoPor
        coleccion.add(producto.copy(creadoPor = autor).aMapa()).await()
    }

    override suspend fun actualizar(producto: Producto): Result<Unit> = enResultado {
        coleccion.document(producto.id).set(producto.aMapa()).await()
    }

    override suspend fun eliminar(id: String): Result<Unit> = enResultado {
        coleccion.document(id).delete().await()
    }

    private fun DocumentSnapshot.aProducto(): Producto? {
        if (!exists()) return null
        return Producto(
            id = id,
            nombre = getString("nombre") ?: "",
            categoria = getString("categoria") ?: "General",
            precio = getDouble("precio") ?: 0.0,
            stock = (getLong("stock") ?: 0L).toInt(),
            emoji = getString("emoji") ?: "🧺",
            descripcion = getString("descripcion") ?: "",
            creadoPor = getString("creadoPor") ?: ""
        )
    }

    private fun Producto.aMapa(): Map<String, Any> = mapOf(
        "nombre" to nombre.trim(),
        "categoria" to categoria.trim(),
        "precio" to precio,
        "stock" to stock,
        "emoji" to emoji,
        "descripcion" to descripcion.trim(),
        "creadoPor" to creadoPor
    )

    private inline fun enResultado(bloque: () -> Unit): Result<Unit> = try {
        bloque()
        Result.success(Unit)
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        Result.failure(e)
    }
}
