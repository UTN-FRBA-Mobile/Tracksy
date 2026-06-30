package com.example.tracksy

import android.app.Application
import com.example.tracksy.notifications.TracksyNotificationManager

class TracksyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        TracksyNotificationManager.createChannels(this)
    }
}
