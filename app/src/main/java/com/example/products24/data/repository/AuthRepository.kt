package com.example.products24.data.repository

import com.example.products24.data.api.AuthApi
import com.example.products24.data.model.LoginRequest
import com.example.products24.data.model.RegisterRequest

class AuthRepository(private val api: AuthApi) {

    suspend fun register(request: RegisterRequest) = api.register(request)

    suspend fun login(request: LoginRequest) = api.login(request)
}
