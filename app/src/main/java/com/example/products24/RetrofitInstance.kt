package com.example.products24

import com.example.products24.data.api.AuthApi
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import android.content.Context
import com.example.products24.data.api.AuthInterceptor
import com.example.products24.data.api.TokenAuthenticator
import java.util.concurrent.TimeUnit
object RetrofitInstance {

    private var retrofit: Retrofit? = null

    fun init(context: Context, baseUrl: String) {
        val logger = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val client = OkHttpClient.Builder()
            .addInterceptor(logger)
            .addInterceptor(AuthInterceptor(context))
            .authenticator(TokenAuthenticator(context))
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()

        retrofit = Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    fun authApi(): AuthApi {
        return retrofit!!.create(AuthApi::class.java)
    }

    fun <T> create(service: Class<T>): T {
        return retrofit!!.create(service)
    }
}
