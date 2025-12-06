package com.example.products24.data.model

data class UserResponse(
    val userID: String,
    val fullName: String,
    val email: String,
    val phoneNumber: String,
    val city: String,
    val street: String,
    val houseNumber: String,
    val role: String
)