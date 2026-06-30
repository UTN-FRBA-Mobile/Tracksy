package com.example.tracksy.recommendations

import android.content.Context
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.example.tracksy.data.local.RecommendationStorage
import com.example.tracksy.data.local.UserPreferencesRepository
import com.example.tracksy.data.repository.TracksyRepository
import com.example.tracksy.notifications.TracksyNotificationManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

class RecommendationWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val prefs = UserPreferencesRepository(applicationContext)
        if (!prefs.notificationsEnabled) return@withContext Result.success()

        runCatching {
            val repo = TracksyRepository()
            val storage = RecommendationStorage(applicationContext)
            val engine = RecommendationEngine()

            val favoritos = repo.getFavoritos().body()?.results ?: emptyList()
            val productos = repo.getProductos(pageSize = 200).body()?.results ?: emptyList()
            val compras = repo.getCompras().body()?.results ?: emptyList()
            val listas = repo.getListas().body()?.results ?: emptyList()

            val ctx = RecommendationContext(
                favoritos = favoritos,
                productos = productos,
                compras = compras,
                listasActivas = listas
            )

            val prevCount = storage.loadVisible().size
            storage.mergeAndSave(engine.evaluate(ctx))
            val newCount = storage.loadVisible().size

            if (newCount > prevCount) {
                TracksyNotificationManager.sendRecommendationsNotification(applicationContext, newCount)
            }
        }

        Result.success()
    }

    companion object {
        private const val WORK_NAME = "tracksy_recommendations"

        fun schedule(context: Context) {
            val request = PeriodicWorkRequestBuilder<RecommendationWorker>(24, TimeUnit.HOURS)
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                )
                .build()
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                request
            )
        }
    }
}
