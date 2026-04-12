package com.example.products24.data.model

import com.google.gson.annotations.SerializedName

data class OrderItemDto(
    @SerializedName("productID") val productID: String,
    @SerializedName("productName") val productName: String,
    @SerializedName("quantity") val quantity: Int,
    @SerializedName("unitPrice") val priceAtOrder: Double,
    val product: OrderProductInfo?,
    var isChecked: Boolean = false
): java.io.Serializable