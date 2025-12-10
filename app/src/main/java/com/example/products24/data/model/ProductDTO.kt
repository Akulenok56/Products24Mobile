package com.example.products24.data.model

data class ProductDto(
    val productID: String,
    val name: String,
    val price: Double,
    val imageUrl: String,
    val description: String,
    val stockQuantity: Int
)