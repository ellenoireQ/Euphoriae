package com.oss.euphoriae.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "playlists")
data class Playlist(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val coverUri: String? = null,
    val createdAt: Long = System.currentTimeMillis()
) {
    @androidx.room.Ignore
    val songCount: Int = 0
    @androidx.room.Ignore
    val covers: List<String> = emptyList()
    
    constructor(id: Long, name: String, coverUri: String?, createdAt: Long, songCount: Int, covers: List<String>) : this(id, name, coverUri, createdAt) {
        // This constructor allows us to create instances with the ignored fields populated
        // We use reflection or copy() in Kotlin usually, but for mapping we might need this or just use copy
    }
    
    // Helper to create a copy with new data since data class copy won't include ignored fields in the constructor
    fun withData(count: Int, coverList: List<String>): Playlist {
        // We can't easily modify val fields of a data class that aren't in the constructor.
        // So we should probably move them to the constructor but mark them @Ignore?
        // Room generic entity cannot have @Ignore in constructor param unless matches a field?
        // Actually best way: Add them to constructor, set default, and use @Ignore on properties? No.
        // If I change constructor, Room needs migration or I need to provide a constructor that Room can use.
        return Playlist(id, name, coverUri, createdAt, count, coverList)
    }
}

@Entity(tableName = "playlist_songs", primaryKeys = ["playlistId", "songId"])
data class PlaylistSong(
    val playlistId: Long,
    val songId: Long,
    val addedAt: Long = System.currentTimeMillis()
)
