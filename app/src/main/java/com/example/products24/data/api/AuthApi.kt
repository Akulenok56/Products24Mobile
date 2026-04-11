package com.example.products24.data.api

import com.example.products24.data.model.AddCartItemDto
import com.example.products24.data.model.CartItemDto
import com.example.products24.data.model.LoginRequest
import com.example.products24.data.model.LoginResponse
import com.example.products24.data.model.OrderDetailsDto
import com.example.products24.data.model.OrderDto
import com.example.products24.data.model.OrderHistoryDto
import com.example.products24.data.model.OrderItemDto
import com.example.products24.data.model.ProductDto
import com.example.products24.data.model.RegisterRequest
import com.example.products24.data.model.UserDto
import com.example.products24.data.model.UserProfileResponse
import com.example.products24.data.model.UserResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

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

    @GET("user/profile")
    suspend fun getProfile(): Response<UserProfileResponse>

    @Multipart
    @POST("user/upload-avatar")
    suspend fun uploadAvatar(
        @Part file: MultipartBody.Part
    ): Response<Map<String, String>>

    @PUT("cart/items/{itemId}/increase")
    suspend fun increase(@Path("itemId") itemId: String): Response<CartItemDto>

    @PUT("cart/items/{itemId}/decrease")
    suspend fun decrease(@Path("itemId") itemId: String): Response<Unit>

    @POST("orders/checkout")
    suspend fun checkout(@Query("address") address: String): Response<Unit>
    @GET("orders/my-orders")
    suspend fun getMyOrders(): Response<List<OrderHistoryDto>>

    @GET("orders/all") // Было Orders/all
    suspend fun getAllOrders(): List<OrderDto>

    // 2. Изменяем регистр здесь
    @GET("orders/pending") // Было Orders/pending
    suspend fun getPendingOrders(): List<OrderDto>



    @PUT("orders/{id}/assembled")
    suspend fun markAsAssembled(@Path("id") orderId: String): Response<Unit>
    @GET("admin/users")
    suspend fun getAdminAllUsers(): List<UserDto>

    @PATCH("admin/users/{id}/role")
    suspend fun updateRole(@Path("id") id: String, @Body role: String): Response<Unit>

    @DELETE("products/admin/{id}")
    suspend fun deleteProduct(@Path("id") id: String): Response<Unit>

    @DELETE("admin/users/{id}") // Путь должен быть таким же, как в MapGroup + MapDelete
    suspend fun deleteUser(@Path("id") id: String): Response<Unit>
    @Multipart
    @POST("products/with-image")
    suspend fun addProductWithImage(
        @Part("name") name: RequestBody,
        @Part("category") category: RequestBody,
        @Part("description") description: RequestBody,
        @Part("price") price: RequestBody,
        @Part("stockQuantity") stock: RequestBody,
        @Part("calories") calories: RequestBody,
        @Part("proteins") proteins: RequestBody,
        @Part("fats") fats: RequestBody,
        @Part("carbs") carbs: RequestBody,
        @Part image: MultipartBody.Part
    ): Response<ProductDto>

    @GET("orders/{id}")
    suspend fun getOrderById(
        @Path("id") id: String
    ): Response<OrderDetailsDto>

    @PUT("orders/{id}/shipped")
    suspend fun markAsShipped(@Path("id") id: String): Response<Unit>

    @PUT("orders/{id}/complete")
    suspend fun markAsComplete(@Path("id") id: String): Response<Unit>


}