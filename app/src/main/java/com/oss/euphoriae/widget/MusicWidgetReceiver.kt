package com.oss.euphoriae.widget

import android.content.Context
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver

/**
 * BroadcastReceiver for the Music Widget
 * This is the entry point for the widget system
 */
class MusicWidgetReceiver : GlanceAppWidgetReceiver() {
    
    override val glanceAppWidget: GlanceAppWidget = MusicWidget()
    
    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        android.util.Log.d("MusicWidgetReceiver", "Widget enabled")
    }
    
    override fun onDisabled(context: Context) {
        super.onDisabled(context)
        android.util.Log.d("MusicWidgetReceiver", "Widget disabled")
    }
}
