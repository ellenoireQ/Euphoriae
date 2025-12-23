package com.oss.euphoriae.data.`class`

import android.content.Context
import android.net.Uri
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer

class EuphoriaePlayer(private val context: Context) {
    
    private var exoPlayer: ExoPlayer? = null
    
    val player: ExoPlayer?
        get() = exoPlayer
    
    val isPlaying: Boolean
        get() = exoPlayer?.isPlaying ?: false
    
    val currentPosition: Long
        get() = exoPlayer?.currentPosition ?: 0L
    
    val duration: Long
        get() = exoPlayer?.duration ?: 0L
    
    val audioSessionId: Int
        get() = exoPlayer?.audioSessionId ?: 0
    
    fun initialize() {
        if (exoPlayer == null) {
            exoPlayer = ExoPlayer.Builder(context).build()
        }
    }
    
    fun setup(
        vararg urls: String,
        playWhenReady: Boolean = true,
        speed: Float = 1f
    ) {
        initialize()
        
        exoPlayer?.apply {
            stop()
            clearMediaItems()
            
            urls.forEach { url ->
                val mediaItem = MediaItem.fromUri(Uri.parse(url))
                addMediaItem(mediaItem)
            }
            
            playbackParameters = PlaybackParameters(speed)
            this.playWhenReady = playWhenReady
            prepare()
        }
    }
    
    fun play() {
        exoPlayer?.play()
    }
    
    fun pause() {
        exoPlayer?.pause()
    }
    
    fun stop() {
        exoPlayer?.stop()
    }
    
    fun seekTo(positionMs: Long) {
        exoPlayer?.seekTo(positionMs)
    }
    
    fun seekToNext() {
        exoPlayer?.seekToNextMediaItem()
    }
    
    fun seekToPrevious() {
        exoPlayer?.seekToPreviousMediaItem()
    }
    
    fun setSpeed(speed: Float) {
        exoPlayer?.playbackParameters = PlaybackParameters(speed)
    }
    
    fun setRepeatMode(repeatMode: Int) {
        exoPlayer?.repeatMode = repeatMode
    }
    
    fun setShuffleModeEnabled(enabled: Boolean) {
        exoPlayer?.shuffleModeEnabled = enabled
    }
    
    fun addListener(listener: Player.Listener) {
        exoPlayer?.addListener(listener)
    }
    
    fun removeListener(listener: Player.Listener) {
        exoPlayer?.removeListener(listener)
    }
    
    fun release() {
        exoPlayer?.release()
        exoPlayer = null
    }
}
