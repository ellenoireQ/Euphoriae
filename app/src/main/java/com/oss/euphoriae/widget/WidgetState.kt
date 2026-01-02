package com.oss.euphoriae.widget

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.appwidget.updateAll
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.FileNotFoundException

/**
 * Widget state keys for DataStore
 */
object WidgetStateKeys {
    val SONG_TITLE = stringPreferencesKey("song_title")
    val SONG_ARTIST = stringPreferencesKey("song_artist")
    val ALBUM_ART_URI = stringPreferencesKey("album_art_uri")
    val IS_PLAYING = booleanPreferencesKey("is_playing")
    val SONG_ID = longPreferencesKey("song_id")
}

/**
 * Data class representing the widget state
 */
data class WidgetState(
    val songTitle: String = "No song playing",
    val songArtist: String = "Euphoriae",
    val albumArtUri: String? = null,
    val isPlaying: Boolean = false,
    val songId: Long = -1L
) {
    companion object {
        fun fromPreferences(prefs: Preferences): WidgetState {
            return WidgetState(
                songTitle = prefs[WidgetStateKeys.SONG_TITLE] ?: "No song playing",
                songArtist = prefs[WidgetStateKeys.SONG_ARTIST] ?: "Euphoriae",
                albumArtUri = prefs[WidgetStateKeys.ALBUM_ART_URI],
                isPlaying = prefs[WidgetStateKeys.IS_PLAYING] ?: false,
                songId = prefs[WidgetStateKeys.SONG_ID] ?: -1L
            )
        }
    }
}

/**
 * Helper object to update widget state from service/viewmodel
 */
object WidgetUpdater {
    
    suspend fun updateWidgetState(
        context: Context,
        songTitle: String,
        songArtist: String,
        albumArtUri: String?,
        isPlaying: Boolean,
        songId: Long
    ) {
        try {
            val manager = GlanceAppWidgetManager(context)
            val glanceIds = manager.getGlanceIds(MusicWidget::class.java)
            
            glanceIds.forEach { glanceId ->
                updateAppWidgetState(context, glanceId) { prefs ->
                    prefs[WidgetStateKeys.SONG_TITLE] = songTitle
                    prefs[WidgetStateKeys.SONG_ARTIST] = songArtist
                    albumArtUri?.let { prefs[WidgetStateKeys.ALBUM_ART_URI] = it }
                        ?: prefs.remove(WidgetStateKeys.ALBUM_ART_URI)
                    prefs[WidgetStateKeys.IS_PLAYING] = isPlaying
                    prefs[WidgetStateKeys.SONG_ID] = songId
                }
            }
            
            MusicWidget().updateAll(context)
        } catch (e: Exception) {
            android.util.Log.e("WidgetUpdater", "Failed to update widget", e)
        }
    }
    
    suspend fun updatePlayingState(context: Context, isPlaying: Boolean) {
        try {
            val manager = GlanceAppWidgetManager(context)
            val glanceIds = manager.getGlanceIds(MusicWidget::class.java)
            
            glanceIds.forEach { glanceId ->
                updateAppWidgetState(context, glanceId) { prefs ->
                    prefs[WidgetStateKeys.IS_PLAYING] = isPlaying
                }
            }
            
            MusicWidget().updateAll(context)
        } catch (e: Exception) {
            android.util.Log.e("WidgetUpdater", "Failed to update playing state", e)
        }
    }
    
    /**
     * Load album art bitmap from URI
     */
    suspend fun loadAlbumArt(context: Context, uriString: String?): Bitmap? {
        if (uriString == null) return null
        
        return withContext(Dispatchers.IO) {
            try {
                val uri = Uri.parse(uriString)
                context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    BitmapFactory.decodeStream(inputStream)
                }
            } catch (e: FileNotFoundException) {
                null
            } catch (e: Exception) {
                android.util.Log.e("WidgetUpdater", "Failed to load album art", e)
                null
            }
        }
    }
}
