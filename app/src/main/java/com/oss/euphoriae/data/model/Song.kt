package com.oss.euphoriae.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "songs")
data class Song(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val artist: String = "Unknown Artist",
    val album: String = "Unknown Album",
    val albumId: Long = 0,
    val duration: Long = 0,
    val data: String = "",
    val albumArtUri: String? = null,
    val mimeType: String? = null
) {
    fun isLossless(): Boolean {
        return when (mimeType?.lowercase()) {
            "audio/flac", 
            "audio/x-flac",
            "audio/wav",
            "audio/x-wav",
            "audio/aiff",
            "audio/x-aiff",
            "audio/alac",
            "audio/x-alac" -> true
            else -> false
        }
    }
    
    fun getFormatName(): String? {
        return when (mimeType?.lowercase()) {
            "audio/flac", "audio/x-flac" -> "FLAC"
            "audio/wav", "audio/x-wav" -> "WAV"
            "audio/aiff", "audio/x-aiff" -> "AIFF"
            "audio/alac", "audio/x-alac" -> "ALAC"
            "audio/mpeg" -> "MP3"
            "audio/aac", "audio/mp4", "audio/x-m4a" -> "AAC"
            "audio/ogg" -> "OGG"
            else -> null
        }
    }
    
    fun formatDuration(): String {
        val minutes = (duration / 1000) / 60
        val seconds = (duration / 1000) % 60
        return String.format("%d:%02d", minutes, seconds)
    }
}
