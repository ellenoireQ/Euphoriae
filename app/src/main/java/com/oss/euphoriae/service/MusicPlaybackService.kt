package com.oss.euphoriae.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.annotation.OptIn
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.oss.euphoriae.MainActivity
import com.oss.euphoriae.engine.AudioEngine
import com.oss.euphoriae.engine.NativeAudioProcessor
import com.oss.euphoriae.engine.NativeRenderersFactory
import com.oss.euphoriae.widget.WidgetUpdater
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

@OptIn(UnstableApi::class)
class MusicPlaybackService : MediaSessionService() {
    
    private var mediaSession: MediaSession? = null
    private var player: ExoPlayer? = null
    private var audioEngine: AudioEngine? = null
    private var renderersFactory: NativeRenderersFactory? = null
    
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    
    private val widgetPlayerListener = object : Player.Listener {
        override fun onIsPlayingChanged(isPlaying: Boolean) {
            updateWidget()
        }
        
        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
            updateWidget()
        }
    }
    
    companion object {
        private const val NOTIFICATION_CHANNEL_ID = "euphoriae_playback_channel"
        private const val TAG = "MusicPlaybackService"
        var crossfadeDurationMs: Long = 0  // 0 = disabled, up to 12000ms
        
        // Widget action constants
        const val ACTION_PLAY_PAUSE = "com.oss.euphoriae.action.PLAY_PAUSE"
        const val ACTION_NEXT = "com.oss.euphoriae.action.NEXT"
        const val ACTION_PREVIOUS = "com.oss.euphoriae.action.PREVIOUS"
    }
    
    override fun onCreate() {
        super.onCreate()
        
        createNotificationChannel()
        initializeAudioEngine()
        
        // Create custom renderers factory with native audio processing
        renderersFactory = audioEngine?.let { NativeRenderersFactory(this, it) }
        
        player = ExoPlayer.Builder(this, renderersFactory ?: return)
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                    .setUsage(C.USAGE_MEDIA)
                    .build(),
                true // handleAudioFocus
            )
            .setHandleAudioBecomingNoisy(true)
            .build()
        
        // Add widget update listener
        player?.addListener(widgetPlayerListener)
        
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        mediaSession = MediaSession.Builder(this, player!!)
            .setSessionActivity(pendingIntent)
            .build()
        
        Log.d(TAG, "MusicPlaybackService created with native audio processing pipeline")
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val result = super.onStartCommand(intent, flags, startId)
        
        when (intent?.action) {
            ACTION_PLAY_PAUSE -> {
                player?.let {
                    if (it.isPlaying) it.pause() else it.play()
                }
            }
            ACTION_NEXT -> {
                player?.let {
                    if (it.hasNextMediaItem()) {
                        it.seekToNext()
                    }
                }
            }
            ACTION_PREVIOUS -> {
                player?.let {
                    if (it.currentPosition > 3000) {
                        it.seekTo(0)
                    } else if (it.hasPreviousMediaItem()) {
                        it.seekToPrevious()
                    }
                }
            }
        }
        
        return result
    }
    
    private fun initializeAudioEngine() {
        try {
            audioEngine = AudioEngine.getInstance().apply {
                create()
            }
            Log.d(TAG, "Native AudioEngine singleton initialized")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize native AudioEngine", e)
        }
    }
    
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                "Music Playback",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows current playing music"
                setShowBadge(false)
            }
            
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
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
        
        // Cleanup native audio engine
        audioEngine?.destroy()
        audioEngine = null
        renderersFactory = null
        
        // Cancel coroutine scope
        serviceScope.cancel()
        
        Log.d(TAG, "MusicPlaybackService destroyed")
        super.onDestroy()
    }
    
    fun getPlayer(): ExoPlayer? = player
    
    fun getAudioSessionId(): Int = player?.audioSessionId ?: 0
    
    fun getAudioEngine(): AudioEngine? = audioEngine
    
    fun getNativeAudioProcessor(): NativeAudioProcessor? = renderersFactory?.getNativeAudioProcessor()
    
    // Native audio effects control
    fun setNativeVolume(volume: Float) {
        audioEngine?.setVolume(volume)
    }
    
    fun setNativeBassBoost(strength: Float) {
        audioEngine?.setBassBoost(strength)
    }
    
    fun setNativeVirtualizer(strength: Float) {
        audioEngine?.setVirtualizer(strength)
    }
    
    fun setNativeEqualizerBand(band: Int, gain: Float) {
        audioEngine?.setEqualizerBand(band, gain)
    }
    
    private fun updateWidget() {
        serviceScope.launch {
            val currentPlayer = player ?: return@launch
            val mediaItem = currentPlayer.currentMediaItem
            val metadata = mediaItem?.mediaMetadata
            
            val songTitle = metadata?.title?.toString() ?: "No song playing"
            val songArtist = metadata?.artist?.toString() ?: "Euphoriae"
            val albumArtUri = metadata?.artworkUri?.toString()
            val isPlaying = currentPlayer.isPlaying
            
            WidgetUpdater.updateWidgetState(
                context = this@MusicPlaybackService,
                songTitle = songTitle,
                songArtist = songArtist,
                albumArtUri = albumArtUri,
                isPlaying = isPlaying,
                songId = mediaItem?.mediaId?.toLongOrNull() ?: -1L
            )
        }
    }
}
