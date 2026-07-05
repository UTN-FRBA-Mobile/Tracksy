package com.example.tracksy.widget

import android.content.Intent
import android.widget.RemoteViewsService

class TracksyWidgetItemsService : RemoteViewsService() {
    override fun onGetViewFactory(intent: Intent): RemoteViewsFactory =
        TracksyWidgetItemsFactory(applicationContext)
}
