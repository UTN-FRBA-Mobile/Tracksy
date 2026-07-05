package com.example.tracksy.notifications

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.example.tracksy.MainActivity

object TracksyNotificationManager {

    const val CHANNEL_PROXIMITY = "tracksy_proximity"
    const val CHANNEL_RECOMMENDATIONS = "tracksy_recommendations"
    const val CHANNEL_LOCATION_SERVICE = "tracksy_location_service"

    private const val NOTIF_ID_RECOMMENDATIONS = 9001
    private const val REQUEST_CODE_PROXIMITY = 1
    private const val REQUEST_CODE_RECOMMENDATIONS = 2

    fun createChannels(context: Context) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        listOf(
            NotificationChannel(
                CHANNEL_PROXIMITY,
                "Supermercados cercanos",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply { description = "Avisos al estar cerca de un supermercado con lista planificada" },

            NotificationChannel(
                CHANNEL_RECOMMENDATIONS,
                "Recomendaciones de productos",
                NotificationManager.IMPORTANCE_LOW
            ).apply { description = "Sugerencias de productos según tu historial de compras" },

            NotificationChannel(
                CHANNEL_LOCATION_SERVICE,
                "Servicio de ubicación",
                NotificationManager.IMPORTANCE_LOW
            ).apply { description = "Activo mientras Tracksy monitorea supermercados cercanos" }
        ).forEach { manager.createNotificationChannel(it) }
    }

    fun buildLocationServiceNotification(context: Context): Notification =
        NotificationCompat.Builder(context, CHANNEL_LOCATION_SERVICE)
            .setSmallIcon(android.R.drawable.ic_menu_mylocation)
            .setContentTitle("Tracksy activo")
            .setContentText("Monitoreando supermercados cercanos")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .build()

    fun sendProximityNotification(
        context: Context,
        supermercadoNombre: String,
        listaNombre: String
    ) {
        val intent = mainActivityIntent(context, REQUEST_CODE_PROXIMITY)
        val notification = NotificationCompat.Builder(context, CHANNEL_PROXIMITY)
            .setSmallIcon(android.R.drawable.ic_menu_mylocation)
            .setContentTitle("Cerca de $supermercadoNombre")
            .setContentText("Tenés una lista planificada: $listaNombre")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(intent)
            .setAutoCancel(true)
            .build()
        notificationManager(context).notify(supermercadoNombre.hashCode(), notification)
    }

    fun sendRecommendationsNotification(context: Context, count: Int) {
        val text = if (count == 1) "Tenés 1 nueva sugerencia de producto"
                   else "Tenés $count nuevas sugerencias de productos"
        val notification = NotificationCompat.Builder(context, CHANNEL_RECOMMENDATIONS)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Recomendaciones para vos")
            .setContentText(text)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setContentIntent(
                mainActivityIntent(context, REQUEST_CODE_RECOMMENDATIONS) {
                    putExtra(MainActivity.EXTRA_OPEN_RECOMMENDATIONS, true)
                }
            )
            .setAutoCancel(true)
            .build()
        notificationManager(context).notify(NOTIF_ID_RECOMMENDATIONS, notification)
    }

    private fun mainActivityIntent(
        context: Context,
        requestCode: Int,
        configure: Intent.() -> Unit = {}
    ): PendingIntent =
        PendingIntent.getActivity(
            context,
            requestCode,
            Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
                configure()
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

    private fun notificationManager(context: Context) =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
}
