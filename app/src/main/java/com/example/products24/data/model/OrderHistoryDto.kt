package com.example.products24.data.model

data class OrderHistoryDto(
    val orderID: String,
    val orderDate: String,
    val status: String,
    val totalAmount: Double,
    val itemsCount: Int
)

data class UserProfileDto(
    val username: String,
    val email: String
)
data class UserProfileResponse(
    val userID: String,
    val fullName: String,
    val email: String,
    val phoneNumber: String?,
    val photoUrl: String?
)