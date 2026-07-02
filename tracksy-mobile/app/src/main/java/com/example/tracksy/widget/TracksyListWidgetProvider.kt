package com.example.tracksy.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager

class TracksyListWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        enqueueRefresh(context)
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        when (intent.action) {
            ACTION_REFRESH -> enqueueRefresh(context)
            ACTION_TOGGLE_ITEM -> {
                val itemId = intent.getIntExtra(EXTRA_ITEM_ID, -1)
                if (itemId == -1) return
                val wasComprado = intent.getBooleanExtra(EXTRA_WAS_COMPRADO, false)
                val data = Data.Builder()
                    .putInt(TracksyWidgetToggleWorker.KEY_ITEM_ID, itemId)
                    .putBoolean(TracksyWidgetToggleWorker.KEY_WAS_COMPRADO, wasComprado)
                    .build()
                val request = OneTimeWorkRequestBuilder<TracksyWidgetToggleWorker>()
                    .setInputData(data)
                    .build()
                WorkManager.getInstance(context.applicationContext).enqueue(request)
            }
        }
    }

    companion object {
        const val ACTION_REFRESH = "com.example.tracksy.widget.ACTION_REFRESH"
        const val ACTION_TOGGLE_ITEM = "com.example.tracksy.widget.ACTION_TOGGLE_ITEM"
        const val EXTRA_ITEM_ID = "extra_item_id"
        const val EXTRA_WAS_COMPRADO = "extra_was_comprado"
        private const val WORK_NAME = "tracksy_widget_refresh"

        fun hasActiveWidgets(context: Context): Boolean {
            val manager = AppWidgetManager.getInstance(context)
            val ids = manager.getAppWidgetIds(ComponentName(context, TracksyListWidgetProvider::class.java))
            return ids.isNotEmpty()
        }

        /** Encola un refresh en background; se puede llamar desde la app o desde el propio widget. */
        fun enqueueRefresh(context: Context) {
            if (!hasActiveWidgets(context)) return
            val request = OneTimeWorkRequestBuilder<TracksyWidgetUpdateWorker>().build()
            WorkManager.getInstance(context.applicationContext)
                .enqueueUniqueWork(WORK_NAME, ExistingWorkPolicy.REPLACE, request)
        }
    }
}
