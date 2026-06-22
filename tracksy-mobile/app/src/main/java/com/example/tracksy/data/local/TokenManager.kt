package com.example.tracksy.data.local

import android.content.Context

class TokenManager(context: Context) {

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    var isDarkMode: Boolean
        get() = prefs.getBoolean(KEY_DARK_MODE, false)
        set(value) { prefs.edit().putBoolean(KEY_DARK_MODE, value).apply() }

    var pendingEmailVerification: Boolean
        get() = prefs.getBoolean(KEY_PENDING_EMAIL_VERIFICATION, false)
        set(value) { prefs.edit().putBoolean(KEY_PENDING_EMAIL_VERIFICATION, value).apply() }

    var profilePhotoUri: String
        get() = prefs.getString(KEY_PROFILE_PHOTO_URI, "").orEmpty()
        set(value) { prefs.edit().putString(KEY_PROFILE_PHOTO_URI, value).apply() }

    companion object {
        private const val PREFS_NAME  = "tracksy_prefs"
        private const val KEY_DARK_MODE = "dark_mode"
        private const val KEY_PENDING_EMAIL_VERIFICATION = "pending_email_verification"
        private const val KEY_PROFILE_PHOTO_URI = "profile_photo_uri"
    }
}
