package com.example.products24.data.model

data class ProductDto(
    val productID: String,
    val name: String,
    val description: String,
    val price: Double,
    val imageUrl: String?,
    val calories: Double,
    val proteins: Double,
    val fats: Double,
    val carbs: Double,
    val stockQuantity: Int
)