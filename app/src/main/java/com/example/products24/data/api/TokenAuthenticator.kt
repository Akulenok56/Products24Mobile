package com.example.products24.data.api

import android.content.Context
import com.example.products24.Session
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route

class TokenAuthenticator(private val context: Context) : Authenticator {
    override fun authenticate(route: Route?, response: Response): Request? {

        Session.clearToken(context)
        return null
    }
}