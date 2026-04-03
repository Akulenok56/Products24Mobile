package com.example.products24.data.model

import com.google.gson.annotations.SerializedName

data class OrderDetailsDto(
    @SerializedName("orderID")
    val orderID: String,

    @SerializedName("userName") // Добавили имя клиента
    val userName: String?,

    @SerializedName("phoneNumber") // Добавили телефон
    val phoneNumber: String?,

    @SerializedName("address") // Добавили полный адрес
    val address: String?,

    @SerializedName("orderDate")
    val orderDate: String,

    @SerializedName("totalAmount")
    val totalAmount: Double,

    @SerializedName("status")
    val status: String,

    @SerializedName("orderItems")
    val orderItems: List<OrderItemDto>
)