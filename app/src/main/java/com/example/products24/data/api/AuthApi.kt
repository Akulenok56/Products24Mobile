package com.example.products24.data.api

import com.example.products24.data.model.AddCartItemDto
import com.example.products24.data.model.CartItemDto
import com.example.products24.data.model.LoginRequest
import com.example.products24.data.model.LoginResponse
import com.example.products24.data.model.ProductDto
import com.example.products24.data.model.RegisterRequest
import com.example.products24.data.model.UserResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface AuthApi {

    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<UserResponse>

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>
    @GET("products")
    suspend fun getProducts(): List<ProductDto>

    @POST("cart/items")
    suspend fun addToCart(
        @Body dto: AddCartItemDto
    ): Response<Unit>
    @GET("cart/items")
    suspend fun getCartItems(): Response<List<CartItemDto>>

    @PUT("cart/items/{itemId}/increase")
    suspend fun increase(@Path("itemId") itemId: String): Response<CartItemDto>

    @PUT("cart/items/{itemId}/decrease")
    suspend fun decrease(@Path("itemId") itemId: String): Response<Unit>

}