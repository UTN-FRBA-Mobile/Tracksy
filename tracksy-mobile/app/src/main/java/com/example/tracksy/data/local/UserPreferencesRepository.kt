package com.example.tracksy.data.local

import android.content.Context

class UserPreferencesRepository(context: Context) {

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    var notificationsEnabled: Boolean
        get() = prefs.getBoolean(KEY_NOTIFICATIONS_ENABLED, true)
        set(value) { prefs.edit().putBoolean(KEY_NOTIFICATIONS_ENABLED, value).apply() }

    var proximityAlertsEnabled: Boolean
        get() = prefs.getBoolean(KEY_PROXIMITY_ALERTS_ENABLED, true)
        set(value) { prefs.edit().putBoolean(KEY_PROXIMITY_ALERTS_ENABLED, value).apply() }

    var proximityDistanceMeters: Int
        get() = prefs.getInt(KEY_PROXIMITY_DISTANCE, DEFAULT_PROXIMITY_DISTANCE)
        set(value) { prefs.edit().putInt(KEY_PROXIMITY_DISTANCE, value).apply() }

    fun lastProximityNotificationTime(supermercadoId: Int): Long =
        prefs.getLong("${KEY_PROXIMITY_LAST_NOTIF}_$supermercadoId", 0L)

    fun recordProximityNotification(supermercadoId: Int) {
        prefs.edit()
            .putLong("${KEY_PROXIMITY_LAST_NOTIF}_$supermercadoId", System.currentTimeMillis())
            .apply()
    }

    companion object {
        private const val PREFS_NAME = "tracksy_user_prefs"
        private const val KEY_NOTIFICATIONS_ENABLED = "notifications_enabled"
        private const val KEY_PROXIMITY_ALERTS_ENABLED = "proximity_alerts_enabled"
        private const val KEY_PROXIMITY_DISTANCE = "proximity_distance_meters"
        private const val KEY_PROXIMITY_LAST_NOTIF = "proximity_last_notif"

        const val DEFAULT_PROXIMITY_DISTANCE = 500
        const val MIN_PROXIMITY_DISTANCE = 100
        const val MAX_PROXIMITY_DISTANCE = 1000

        // 6 hours between repeated alerts for the same supermarket
        const val PROXIMITY_COOLDOWN_MS = 6 * 60 * 60 * 1000L
    }
}
