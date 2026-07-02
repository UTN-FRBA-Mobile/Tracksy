package com.example.tracksy.widget

import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import androidx.core.content.ContextCompat
import com.example.tracksy.R

/**
 * Arma cada fila de la lista de productos del widget. Corre sincrónicamente en el proceso
 * del widget host, por lo que solo lee el snapshot ya cacheado por TracksyWidgetUpdateWorker
 * (nunca hace red acá).
 */
class TracksyWidgetItemsFactory(private val context: Context) : RemoteViewsService.RemoteViewsFactory {

    private var items: List<WidgetProductoUi> = emptyList()

    override fun onCreate() {
        items = WidgetDataStore.load(context)?.items ?: emptyList()
    }

    override fun onDataSetChanged() {
        items = WidgetDataStore.load(context)?.items ?: emptyList()
    }

    override fun onDestroy() {
        items = emptyList()
    }

    override fun getCount(): Int = items.size

    override fun getViewAt(position: Int): RemoteViews {
        val item = items[position]
        return RemoteViews(context.packageName, R.layout.widget_list_item).apply {
            setImageViewResource(
                R.id.item_status_icon,
                if (item.comprado) R.drawable.widget_status_done else R.drawable.widget_status_pending
            )
            setTextViewText(R.id.item_name, item.nombre)
            setTextColor(
                R.id.item_name,
                ContextCompat.getColor(
                    context,
                    if (item.comprado) R.color.widget_text_secondary else R.color.widget_text_primary
                )
            )
            setTextViewText(R.id.item_qty, if (item.cantidad > 1) "x${item.cantidad}" else "")

            val fillInIntent = Intent().apply {
                putExtra(TracksyListWidgetProvider.EXTRA_ITEM_ID, item.itemId)
                putExtra(TracksyListWidgetProvider.EXTRA_WAS_COMPRADO, item.comprado)
            }
            setOnClickFillInIntent(R.id.item_row_root, fillInIntent)
        }
    }

    override fun getLoadingView(): RemoteViews? = null
    override fun getViewTypeCount(): Int = 1
    override fun getItemId(position: Int): Long = position.toLong()
    override fun hasStableIds(): Boolean = true
}
