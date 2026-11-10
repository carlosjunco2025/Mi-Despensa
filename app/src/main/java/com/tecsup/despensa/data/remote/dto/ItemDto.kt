package com.tecsup.despensa.data.remote.dto

import kotlinx.serialization.Serializable

/**
 * DTO basado en la respuesta real de DummyJSON.
 * brand puede faltar en la categoria groceries, por eso es nullable con default.
 */
@Serializable
data class ItemDto(
    val id: Int,
    val title: String,
    val description: String = "",
    val category: String = "",
    val price: Double = 0.0,
    val discountPercentage: Double = 0.0,
    val rating: Double = 0.0,
    val stock: Int = 0,
    val tags: List<String> = emptyList(),
    val brand: String? = null,
    val sku: String = "",
    val weight: Int = 0,
    val warrantyInformation: String = "",
    val shippingInformation: String = "",
    val availabilityStatus: String = "",
    val returnPolicy: String = "",
    val minimumOrderQuantity: Int = 0,
    val images: List<String> = emptyList(),
    val thumbnail: String? = null
)

@Serializable
data class ProductosResponse(
    val products: List<ItemDto> = emptyList(),
    val total: Int = 0,
    val skip: Int = 0,
    val limit: Int = 0
)
