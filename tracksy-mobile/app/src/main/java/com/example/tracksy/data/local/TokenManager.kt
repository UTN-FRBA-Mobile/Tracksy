package com.example.tracksy.data.local

import android.content.Context

class TokenManager(context: Context) {

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    var isDarkMode: Boolean
        get() = prefs.getBoolean(KEY_DARK_MODE, false)
        set(value) { prefs.edit().putBoolean(KEY_DARK_MODE, value).apply() }

    companion object {
        private const val PREFS_NAME  = "tracksy_prefs"
        private const val KEY_DARK_MODE = "dark_mode"
    }
}
