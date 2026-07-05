package com.example.tracksy.widget

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.tracksy.R
import com.example.tracksy.data.repository.TracksyRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Marca/desmarca un producto como comprado desde el widget. Aplica el cambio de forma
 * optimista en el cache local (refresco instantáneo del widget) y lo sincroniza con el
 * backend; si falla, revierte el cache y vuelve a refrescar.
 */
class TracksyWidgetToggleWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val itemId = inputData.getInt(KEY_ITEM_ID, -1)
        if (itemId == -1) return@withContext Result.failure()
        val wasComprado = inputData.getBoolean(KEY_WAS_COMPRADO, false)

        val original = WidgetDataStore.load(applicationContext) ?: return@withContext Result.failure()
        val listaId = original.id

        val optimistic = original.copy(
            items = original.items.map {
                if (it.itemId == itemId) it.copy(comprado = !wasComprado) else it
            }
        ).let { it.copy(pendientesCount = it.items.count { p -> !p.comprado }) }
        WidgetDataStore.save(applicationContext, optimistic)
        notifyWidgets(applicationContext)

        val success = runCatching {
            val repo = TracksyRepository()
            val estados = repo.getEstadosProducto().body()?.results.orEmpty()
            val nuevoEstadoId = estados.firstOrNull { estado ->
                val nombre = estado.nombre.lowercase()
                if (wasComprado) nombre.contains("pendiente") else nombre.contains("comprad")
            }?.id ?: return@runCatching false
            repo.updateItem(listaId, itemId, mapOf("estado" to nuevoEstadoId)).isSuccessful
        }.getOrDefault(false)

        if (!success) {
            WidgetDataStore.save(applicationContext, original)
            notifyWidgets(applicationContext)
            return@withContext Result.failure()
        }

        // Re-sincroniza contra el backend para que ambos widgets/app converjan al mismo estado.
        TracksyListWidgetProvider.enqueueRefresh(applicationContext)
        Result.success()
    }

    private fun notifyWidgets(context: Context) {
        val manager = AppWidgetManager.getInstance(context)
        val ids = manager.getAppWidgetIds(ComponentName(context, TracksyListWidgetProvider::class.java))
        if (ids.isNotEmpty()) manager.notifyAppWidgetViewDataChanged(ids, R.id.widget_items_list)
    }

    companion object {
        const val KEY_ITEM_ID = "item_id"
        const val KEY_WAS_COMPRADO = "was_comprado"
    }
}
