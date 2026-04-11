package com.example.products24.data.model

import com.google.gson.annotations.SerializedName

data class UserDto(
    @SerializedName("userID")
    val userId: String,
    val fullName: String,
    val email: String,
    val role: String
)