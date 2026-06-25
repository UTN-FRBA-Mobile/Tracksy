package com.example.tracksy.location

import android.app.Service
import android.content.Intent
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.IBinder
import com.example.tracksy.data.local.UserPreferencesRepository
import com.example.tracksy.notifications.TracksyNotificationManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

class LocationService : Service() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private lateinit var locationManager: LocationManager
    private lateinit var prefs: UserPreferencesRepository

    private val locationListener = LocationListener { location ->
        serviceScope.launch { checkProximity(location) }
    }

    override fun onCreate() {
        super.onCreate()
        prefs = UserPreferencesRepository(this)
        locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
        startForeground(FOREGROUND_NOTIF_ID, TracksyNotificationManager.buildLocationServiceNotification(this))
        requestLocationUpdates()
    }

    @Suppress("MissingPermission")
    private fun requestLocationUpdates() {
        listOf(LocationManager.GPS_PROVIDER, LocationManager.NETWORK_PROVIDER).forEach { provider ->
            runCatching {
                locationManager.requestLocationUpdates(provider, MIN_TIME_MS, MIN_DISTANCE_M, locationListener)
            }
        }
    }

    private suspend fun checkProximity(userLocation: Location) {
        if (!prefs.proximityAlertsEnabled) return
        val threshold = prefs.proximityDistanceMeters.toFloat()

        ProximityTargets.targets.forEach { target ->
            val distance = haversineMeters(
                userLocation.latitude, userLocation.longitude,
                target.latitud, target.longitud
            )
            if (distance <= threshold && !isOnCooldown(target.supermercadoId)) {
                prefs.recordProximityNotification(target.supermercadoId)
                TracksyNotificationManager.sendProximityNotification(this, target.nombre, target.listaNombre)
            }
        }
    }

    private fun isOnCooldown(supermercadoId: Int): Boolean =
        System.currentTimeMillis() - prefs.lastProximityNotificationTime(supermercadoId) <
                UserPreferencesRepository.PROXIMITY_COOLDOWN_MS

    private fun haversineMeters(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Float {
        val r = 6_371_000.0
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2).pow(2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) * sin(dLon / 2).pow(2)
        return (2 * r * atan2(sqrt(a), sqrt(1 - a))).toFloat()
    }

    override fun onDestroy() {
        super.onDestroy()
        locationManager.removeUpdates(locationListener)
        serviceScope.cancel()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        private const val FOREGROUND_NOTIF_ID = 1001
        private const val MIN_TIME_MS = 60_000L  // 1 minute between updates
        private const val MIN_DISTANCE_M = 50f   // 50 meters minimum movement
    }
}
