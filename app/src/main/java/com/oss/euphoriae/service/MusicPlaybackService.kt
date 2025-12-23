package com.oss.euphoriae.service

import android.app.PendingIntent
import android.content.Intent
import android.net.Uri
import androidx.annotation.OptIn
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.oss.euphoriae.MainActivity
import com.oss.euphoriae.data.model.Song

@OptIn(UnstableApi::class)
class MusicPlaybackService : MediaSessionService() {
    
    private var mediaSession: MediaSession? = null
    private var player: ExoPlayer? = null
    
    override fun onCreate() {
        super.onCreate()
        
        // Create ExoPlayer with audio attributes
        player = ExoPlayer.Builder(this)
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                    .setUsage(C.USAGE_MEDIA)
                    .build(),
                true // handleAudioFocus
            )
            .setHandleAudioBecomingNoisy(true) // Pause when headphones unplugged
            .build()
        
        // Create pending intent for notification click
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        // Create MediaSession
        mediaSession = MediaSession.Builder(this, player!!)
            .setSessionActivity(pendingIntent)
            .build()
    }
    
    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {
        return mediaSession
    }
    
    override fun onTaskRemoved(rootIntent: Intent?) {
        val player = mediaSession?.player
        if (player?.playWhenReady != true || player.mediaItemCount == 0) {
            stopSelf()
        }
    }
    
    override fun onDestroy() {
        mediaSession?.run {
            player.release()
            release()
        }
        mediaSession = null
        player = null
        super.onDestroy()
    }
    
    fun playSong(song: Song) {
        val mediaItem = MediaItem.Builder()
            .setUri(Uri.parse(song.data))
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setTitle(song.title)
                    .setArtist(song.artist)
                    .setAlbumTitle(song.album)
                    .setArtworkUri(song.albumArtUri?.let { Uri.parse(it) })
                    .build()
            )
            .build()
        
        player?.apply {
            setMediaItem(mediaItem)
            prepare()
            play()
        }
    }
    
    fun getAudioSessionId(): Int {
        return player?.audioSessionId ?: 0
    }
    
    companion object {
        private const val TAG = "MusicPlaybackService"
    }
}
