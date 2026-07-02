package com.example.tracksy.widget

import android.content.Context
import com.google.gson.Gson

data class WidgetProductoUi(
    val itemId: Int,
    val nombre: String,
    val cantidad: Int,
    val comprado: Boolean
)

data class WidgetListaUi(
    val id: Int,
    val nombre: String,
    val totalItems: Int,
    val pendientesCount: Int,
    val items: List<WidgetProductoUi>
)

/**
 * Cache local para el widget: TracksyWidgetUpdateWorker escribe el snapshot de la lista
 * activa acá, y TracksyWidgetItemsFactory lo lee de forma sincrónica (sin red) al armar
 * cada fila del RemoteViewsService.
 */
object WidgetDataStore {
    private const val PREFS_NAME = "tracksy_widget_prefs"
    private const val KEY_DATA = "widget_lista_data"
    private val gson = Gson()

    fun save(context: Context, data: WidgetListaUi?) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        if (data == null) {
            prefs.edit().remove(KEY_DATA).apply()
        } else {
            prefs.edit().putString(KEY_DATA, gson.toJson(data)).apply()
        }
    }

    fun load(context: Context): WidgetListaUi? {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val json = prefs.getString(KEY_DATA, null) ?: return null
        return runCatching { gson.fromJson(json, WidgetListaUi::class.java) }.getOrNull()
    }
}
