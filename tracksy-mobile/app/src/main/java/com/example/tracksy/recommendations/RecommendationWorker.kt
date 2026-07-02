package com.example.tracksy.recommendations

import android.content.Context
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
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
        if (!prefs.notificationsEnabled) {
            if (DEMO_MODE) scheduleNext(applicationContext)
            return@withContext Result.success()
        }

        val outcome = runCatching {
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

            // Modo demo: se resetea lo "ya visto" (no lo dismisseado por el usuario,
            // eso se sigue respetando) antes de evaluar, para que las sugerencias
            // reales que arma el motor cada ciclo cuenten de nuevo como nuevas y
            // disparen la notificación, en vez de simular un contador.
            if (DEMO_MODE) storage.clearVisibleKeepDismissed()

            val prevVisible = storage.loadVisible()
            storage.mergeAndSave(engine.evaluate(ctx))
            val afterVisible = storage.loadVisible()
            val delta = afterVisible.size - prevVisible.size

            if (delta > 0) {
                TracksyNotificationManager.sendRecommendationsNotification(applicationContext, delta)
            }
        }

        if (DEMO_MODE) scheduleNext(applicationContext)
        if (outcome.isFailure) Result.retry() else Result.success()
    }

    companion object {
        private const val WORK_NAME = "tracksy_recommendations"

        // ── Modo demo ────────────────────────────────────────────────────────
        // Con DEMO_MODE = true, en lugar de correr cada 24hs, el worker se
        // reprograma solo cada DEMO_INTERVAL y siempre dispara la notificación,
        // para poder exhibirla en una presentación sin esperar ni depender de
        // que existan sugerencias nuevas reales.
        // IMPORTANTE: volver a false antes de pasar a producción.
        const val DEMO_MODE = true
        private const val DEMO_INTERVAL_MINUTES = 1L

        fun schedule(context: Context) {
            if (DEMO_MODE) {
                scheduleNext(context)
                return
            }
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

        private fun scheduleNext(context: Context) {
            val request = OneTimeWorkRequestBuilder<RecommendationWorker>()
                .setInitialDelay(DEMO_INTERVAL_MINUTES, TimeUnit.MINUTES)
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                )
                .build()
            WorkManager.getInstance(context).enqueueUniqueWork(
                WORK_NAME,
                ExistingWorkPolicy.REPLACE,
                request
            )
        }
    }
}
