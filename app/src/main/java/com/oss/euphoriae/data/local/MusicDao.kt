package com.oss.euphoriae.data.local

import androidx.room.*
import com.oss.euphoriae.data.model.Playlist
import com.oss.euphoriae.data.model.PlaylistSong
import com.oss.euphoriae.data.model.Song
import kotlinx.coroutines.flow.Flow

@Dao
interface MusicDao {
    @Query("SELECT * FROM songs ORDER BY title ASC")
    fun getAllSongs(): Flow<List<Song>>
    
    @Query("SELECT * FROM songs WHERE id = :songId")
    suspend fun getSongById(songId: Long): Song?
    
    @Query("SELECT * FROM songs WHERE title LIKE '%' || :query || '%' OR artist LIKE '%' || :query || '%'")
    fun searchSongs(query: String): Flow<List<Song>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSong(song: Song): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSongs(songs: List<Song>)
    
    @Delete
    suspend fun deleteSong(song: Song)
    
    @Query("DELETE FROM songs")
    suspend fun deleteAllSongs()
    
    @Query("SELECT * FROM playlists ORDER BY createdAt DESC")
    fun getAllPlaylists(): Flow<List<Playlist>>
    
    @Query("SELECT * FROM playlists WHERE id = :playlistId")
    suspend fun getPlaylistById(playlistId: Long): Playlist?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaylist(playlist: Playlist): Long
    
    @Delete
    suspend fun deletePlaylist(playlist: Playlist)
    
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addSongToPlaylist(playlistSong: PlaylistSong)
    
    @Query("DELETE FROM playlist_songs WHERE playlistId = :playlistId AND songId = :songId")
    suspend fun removeSongFromPlaylist(playlistId: Long, songId: Long)
    
    @Query("SELECT s.* FROM songs s INNER JOIN playlist_songs ps ON s.id = ps.songId WHERE ps.playlistId = :playlistId ORDER BY ps.addedAt DESC")
    fun getSongsInPlaylist(playlistId: Long): Flow<List<Song>>
}
