package com.example.products24

import android.content.Context

object Session {
    private const val PREFS = "auth_prefs"
    private const val KEY_TOKEN = "token"

    fun saveToken(context: Context, token: String) {
        val pref = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        pref.edit().putString(KEY_TOKEN, token).apply()
    }

    fun getToken(context: Context): String? {
        val pref = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        return pref.getString(KEY_TOKEN, null)
    }

    fun clearToken(context: Context) {
        val pref = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        pref.edit().remove(KEY_TOKEN).apply()
    }
}