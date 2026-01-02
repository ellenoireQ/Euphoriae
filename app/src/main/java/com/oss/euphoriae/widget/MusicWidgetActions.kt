package com.oss.euphoriae.widget

import android.content.Context
import android.content.Intent
import androidx.glance.GlanceId
import androidx.glance.action.Action
import androidx.glance.action.ActionParameters
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import com.oss.euphoriae.service.MusicPlaybackService

/**
 * Widget actions for playback control
 * Uses Intent-based approach to avoid BroadcastReceiver binding issues
 */
object MusicWidgetActions {
    
    fun playPauseAction(): Action = actionRunCallback<PlayPauseActionCallback>()
    
    fun nextAction(): Action = actionRunCallback<NextActionCallback>()
    
    fun previousAction(): Action = actionRunCallback<PreviousActionCallback>()
}

/**
 * Play/Pause action callback
 */
class PlayPauseActionCallback : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        val intent = Intent(context, MusicPlaybackService::class.java).apply {
            action = MusicPlaybackService.ACTION_PLAY_PAUSE
        }
        context.startService(intent)
    }
}

/**
 * Next track action callback
 */
class NextActionCallback : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        val intent = Intent(context, MusicPlaybackService::class.java).apply {
            action = MusicPlaybackService.ACTION_NEXT
        }
        context.startService(intent)
    }
}

/**
 * Previous track action callback
 */
class PreviousActionCallback : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        val intent = Intent(context, MusicPlaybackService::class.java).apply {
            action = MusicPlaybackService.ACTION_PREVIOUS
        }
        context.startService(intent)
    }
}
