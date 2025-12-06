package com.example.products24.data.api

import com.example.products24.data.model.LoginRequest
import com.example.products24.data.model.LoginResponse
import com.example.products24.data.model.RegisterRequest
import com.example.products24.data.model.UserResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApi {

    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<UserResponse>

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>
}