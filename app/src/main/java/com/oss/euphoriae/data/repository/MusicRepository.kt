package com.oss.euphoriae.data.repository

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import com.oss.euphoriae.data.local.MusicDao
import com.oss.euphoriae.data.model.Playlist
import com.oss.euphoriae.data.model.PlaylistSong
import com.oss.euphoriae.data.model.Song
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class MusicRepository(
    private val context: Context,
    private val musicDao: MusicDao
) {
    
    fun getAllSongs(): Flow<List<Song>> = musicDao.getAllSongs()
    
    fun searchSongs(query: String): Flow<List<Song>> = musicDao.searchSongs(query)
    
    suspend fun getSongById(songId: Long): Song? = musicDao.getSongById(songId)
    
    suspend fun insertSong(song: Song): Long = musicDao.insertSong(song)
    
    suspend fun deleteSong(song: Song) = musicDao.deleteSong(song)
    
    fun getAllPlaylists(): Flow<List<Playlist>> = musicDao.getAllPlaylists()
    
    suspend fun getPlaylistById(playlistId: Long): Playlist? = musicDao.getPlaylistById(playlistId)
    
    suspend fun createPlaylist(name: String): Long {
        val playlist = Playlist(name = name)
        return musicDao.insertPlaylist(playlist)
    }
    
    suspend fun deletePlaylist(playlist: Playlist) = musicDao.deletePlaylist(playlist)
    
    suspend fun addSongToPlaylist(playlistId: Long, songId: Long) {
        val playlistSong = PlaylistSong(playlistId = playlistId, songId = songId)
        musicDao.addSongToPlaylist(playlistSong)
    }
    
    suspend fun removeSongFromPlaylist(playlistId: Long, songId: Long) {
        musicDao.removeSongFromPlaylist(playlistId, songId)
    }
    
    fun getSongsInPlaylist(playlistId: Long): Flow<List<Song>> = musicDao.getSongsInPlaylist(playlistId)
    
    suspend fun scanAndImportMusic(): Int = withContext(Dispatchers.IO) {
        val songs = mutableListOf<Song>()
        
        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.MIME_TYPE
        )
        
        val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0"
        val sortOrder = "${MediaStore.Audio.Media.TITLE} ASC"
        
        context.contentResolver.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            projection,
            selection,
            null,
            sortOrder
        )?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val titleColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
            val artistColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
            val albumColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
            val albumIdColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)
            val durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
            val dataColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
            val mimeTypeColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.MIME_TYPE)
            
            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                val title = cursor.getString(titleColumn) ?: "Unknown"
                val artist = cursor.getString(artistColumn) ?: "Unknown Artist"
                val album = cursor.getString(albumColumn) ?: "Unknown Album"
                val albumId = cursor.getLong(albumIdColumn)
                val duration = cursor.getLong(durationColumn)
                val data = cursor.getString(dataColumn) ?: ""
                val mimeType = cursor.getString(mimeTypeColumn)
                
                val albumArtUri = ContentUris.withAppendedId(
                    Uri.parse("content://media/external/audio/albumart"),
                    albumId
                ).toString()
                
                val song = Song(
                    id = id,
                    title = title,
                    artist = artist,
                    album = album,
                    duration = duration,
                    data = data,
                    albumArtUri = albumArtUri,
                    mimeType = mimeType
                )
                songs.add(song)
            }
        }
        
        if (songs.isNotEmpty()) {
            musicDao.deleteAllSongs()
            musicDao.insertSongs(songs)
        }
        
        songs.size
    }
    
    suspend fun refreshLibrary(): Int = scanAndImportMusic()
}
