package com.example.products24.data.model

import com.google.gson.annotations.SerializedName

data class OrderDto(
    @SerializedName("orderID") val orderID: String,
    @SerializedName("userID") val userID: String,
    @SerializedName("userName") val userName: String,
    @SerializedName("addressDelivery") val addressDelivery: String,
    @SerializedName("status") val status: String,
    @SerializedName("totalAmount") val totalAmount: Double,
    @SerializedName("phoneNumber") val phoneNumber: String?,
    @SerializedName("orderDate") val orderDate: String,
    val courierName: String?,
    @SerializedName("stockQuantity") val stockQuantity: String,
    @SerializedName("items") val items: List<OrderItemDto> = emptyList()
): java.io.Serializable
