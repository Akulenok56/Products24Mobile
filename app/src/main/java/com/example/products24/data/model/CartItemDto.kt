package com.example.products24.data.model

data class CartItemDto(
    val cartItemID: String,
    val productID: String,
    val productName: String,
    val price: Double,
    val quantity: Int,
    val imageUrl: String?
)