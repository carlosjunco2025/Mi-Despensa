package com.tecsup.despensa.data.remote

import com.tecsup.despensa.data.remote.dto.ItemDto
import com.tecsup.despensa.data.remote.dto.ProductosResponse
import retrofit2.http.GET
import retrofit2.http.Path

interface ApiService {
    @GET("products/category/groceries")
    suspend fun listarProductos(): ProductosResponse

    @GET("products/{id}")
    suspend fun obtenerProducto(@Path("id") id: Int): ItemDto
}
