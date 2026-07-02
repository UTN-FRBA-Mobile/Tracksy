package com.example.tracksy.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.RemoteViews
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.tracksy.MainActivity
import com.example.tracksy.R
import com.example.tracksy.data.models.ListaCompra
import com.example.tracksy.data.repository.TracksyRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Refresca todas las instancias del widget "Mi lista activa". Corre en background
 * (background propio de CoroutineWorker) para no bloquear el broadcast del AppWidgetProvider,
 * siguiendo el mismo patrón que RecommendationWorker.
 */
class TracksyWidgetUpdateWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val manager = AppWidgetManager.getInstance(applicationContext)
        val ids = manager.getAppWidgetIds(ComponentName(applicationContext, TracksyListWidgetProvider::class.java))
        if (ids.isEmpty()) return@withContext Result.success()

        if (FirebaseAuth.getInstance().currentUser == null) {
            showMessage(applicationContext, manager, ids, R.string.widget_login_required)
            return@withContext Result.success()
        }

        val outcome = runCatching {
            TracksyRepository().getListas().body()?.results.orEmpty()
        }

        val listas = outcome.getOrNull()
        if (listas == null) {
            showMessage(applicationContext, manager, ids, R.string.widget_load_error)
            return@withContext Result.retry()
        }
        if (listas.isEmpty()) {
            showMessage(applicationContext, manager, ids, R.string.widget_no_lists)
            return@withContext Result.success()
        }

        // La "lista activa" es la que tiene más ítems pendientes de comprar.
        val lista = listas.maxByOrNull { it.pendientes().size } ?: listas.first()
        val data = WidgetListaUi(
            id = lista.id,
            nombre = lista.nombre,
            totalItems = lista.items.size,
            pendientesCount = lista.pendientes().size,
            items = lista.items.map { WidgetProductoUi(it.id, it.productoNombre, it.cantidad, it.esComprado()) }
        )
        WidgetDataStore.save(applicationContext, data)

        ids.forEach { manager.updateAppWidget(it, buildContentViews(applicationContext, it, data)) }
        manager.notifyAppWidgetViewDataChanged(ids, R.id.widget_items_list)
        Result.success()
    }

    companion object {
        private fun ListaCompra.pendientes() = items.filterNot { it.esComprado() }
        private fun com.example.tracksy.data.models.ItemProducto.esComprado() =
            estadoNombre.lowercase().contains("comprad")

        private fun showMessage(
            context: Context,
            manager: AppWidgetManager,
            ids: IntArray,
            messageRes: Int
        ) {
            WidgetDataStore.save(context, null)
            ids.forEach { manager.updateAppWidget(it, buildMessageViews(context, it, messageRes)) }
            manager.notifyAppWidgetViewDataChanged(ids, R.id.widget_items_list)
        }

        /** Abre la app; si se pasa un listaId, MainActivity navega directo al detalle de esa lista. */
        private fun openAppPendingIntent(context: Context, listaId: Int?) = PendingIntent.getActivity(
            context, 0,
            Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
                if (listaId != null) putExtra(MainActivity.EXTRA_WIDGET_LISTA_ID, listaId)
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        private fun refreshPendingIntent(context: Context) = PendingIntent.getBroadcast(
            context, 0,
            Intent(context, TracksyListWidgetProvider::class.java).apply {
                action = TracksyListWidgetProvider.ACTION_REFRESH
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        /**
         * Template de PendingIntent para las filas de la lista: cada fila completa sus extras
         * (itemId, wasComprado) vía RemoteViews.setOnClickFillInIntent en el Factory. Debe ser
         * mutable: el sistema necesita fusionarle el fill-in intent de cada fila.
         */
        private fun toggleItemPendingIntentTemplate(context: Context) = PendingIntent.getBroadcast(
            context, 0,
            Intent(context, TracksyListWidgetProvider::class.java).apply {
                action = TracksyListWidgetProvider.ACTION_TOGGLE_ITEM
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        )

        /** Intent único por widgetId: evita que el sistema reuse el adapter entre instancias. */
        private fun itemsServiceIntent(context: Context, appWidgetId: Int) =
            Intent(context, TracksyWidgetItemsService::class.java).apply {
                data = Uri.parse("tracksy://widget/$appWidgetId")
            }

        private fun baseViews(context: Context, appWidgetId: Int, listaId: Int?) =
            RemoteViews(context.packageName, R.layout.widget_lista_activa).apply {
                setOnClickPendingIntent(R.id.widget_root, openAppPendingIntent(context, listaId))
                setOnClickPendingIntent(R.id.widget_refresh, refreshPendingIntent(context))
                setRemoteAdapter(R.id.widget_items_list, itemsServiceIntent(context, appWidgetId))
                setPendingIntentTemplate(R.id.widget_items_list, toggleItemPendingIntentTemplate(context))
                setEmptyView(R.id.widget_items_list, R.id.widget_empty_state)
            }

        private fun buildMessageViews(context: Context, appWidgetId: Int, messageRes: Int) =
            baseViews(context, appWidgetId, listaId = null).apply {
                setTextViewText(R.id.widget_list_name, context.getString(R.string.widget_default_title))
                setTextViewText(R.id.widget_pending_count, "")
                setTextViewText(R.id.widget_empty_state, context.getString(messageRes))
            }

        private fun buildContentViews(context: Context, appWidgetId: Int, data: WidgetListaUi) =
            baseViews(context, appWidgetId, listaId = data.id).apply {
                setTextViewText(R.id.widget_list_name, data.nombre)
                setTextViewText(R.id.widget_pending_count, "${data.pendientesCount} de ${data.totalItems} pendientes")
                setTextViewText(R.id.widget_empty_state, context.getString(R.string.widget_no_lists))
            }
    }
}
