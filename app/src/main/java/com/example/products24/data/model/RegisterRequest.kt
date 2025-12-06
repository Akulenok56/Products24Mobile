package com.example.products24.data.model

data class RegisterRequest(
    val fullName: String,
    val email: String,
    val phoneNumber: String,
    val password: String,
    val city: String,
    val street: String,
    val houseNumber: String
)