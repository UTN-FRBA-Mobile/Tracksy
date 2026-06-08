package com.example.tracksy.data.local

import android.content.Context

class TokenManager(context: Context) {

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    var accessToken: String?
        get() = prefs.getString(KEY_ACCESS, null)
        set(value) = prefs.edit().putString(KEY_ACCESS, value).apply()

    var refreshToken: String?
        get() = prefs.getString(KEY_REFRESH, null)
        set(value) = prefs.edit().putString(KEY_REFRESH, value).apply()

    var isDarkMode: Boolean
        get() = prefs.getBoolean(KEY_DARK_MODE, false)
        set(value) { prefs.edit().putBoolean(KEY_DARK_MODE, value).apply() }

    fun isLoggedIn() = !accessToken.isNullOrEmpty()

    fun clear() = prefs.edit().remove(KEY_ACCESS).remove(KEY_REFRESH).apply()

    companion object {
        private const val PREFS_NAME  = "tracksy_prefs"
        private const val KEY_ACCESS  = "access_token"
        private const val KEY_REFRESH = "refresh_token"
        private const val KEY_DARK_MODE = "dark_mode"
    }
}
