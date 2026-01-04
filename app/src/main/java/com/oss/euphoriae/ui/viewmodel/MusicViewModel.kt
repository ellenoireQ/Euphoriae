package com.oss.euphoriae.ui.viewmodel

import android.app.Application
import android.content.ComponentName
import android.content.ContentUris
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import com.oss.euphoriae.EuphoriaeApp
import com.oss.euphoriae.data.`class`.AudioEffectsManager
import com.oss.euphoriae.data.model.Lyrics
import com.oss.euphoriae.data.model.Playlist
import com.oss.euphoriae.data.model.Song
import com.oss.euphoriae.engine.AudioEngine
import com.oss.euphoriae.service.MusicPlaybackService
import com.oss.euphoriae.widget.QueueSongInfo
import com.oss.euphoriae.widget.WidgetQueueManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class MusicUiState(
    val songs: List<Song> = emptyList(),
    val playlists: List<Playlist> = emptyList(),
    val playlistSongs: List<Song> = emptyList(),
    val queue: List<Song> = emptyList(),
    val currentQueueIndex: Int = -1,
    val currentSong: Song? = null,
    val isPlaying: Boolean = false,
    val currentPosition: Long = 0L,
    val duration: Long = 0L,
    val progress: Float = 0f,
    val isLoading: Boolean = false,
    val isScanning: Boolean = false,
    val error: String? = null,
    val searchQuery: String = "",
    val isShuffleOn: Boolean = false,
    val repeatMode: Int = 0,
    val tempo: Float = 1.0f,
    val pitch: Float = 0.0f,
    val albums: List<com.oss.euphoriae.data.model.Album> = emptyList(),
    val lyrics: Lyrics? = null,
    val currentLyricIndex: Int = -1
)

class MusicViewModel(application: Application) : AndroidViewModel(application) {
    
    private val repository = (application as EuphoriaeApp).musicRepository
    
    val audioEffectsManager = AudioEffectsManager()
    private var audioEffectsInitialized = false
    
    // Native audio engine - create our own instance for UI control
    private var _audioEngine: AudioEngine? = null
    val audioEngine: AudioEngine?
        get() = _audioEngine
    
    private var controllerFuture: ListenableFuture<MediaController>? = null
    private var mediaController: MediaController? = null
    
    private val _uiState = MutableStateFlow(MusicUiState())
    val uiState: StateFlow<MusicUiState> = _uiState.asStateFlow()
    
    private val _searchQuery = MutableStateFlow("")
    
    private var progressJob: Job? = null
    
    private val playerListener = object : Player.Listener {
        override fun onIsPlayingChanged(isPlaying: Boolean) {
            _uiState.update { it.copy(isPlaying = isPlaying) }
            if (isPlaying) {
                startProgressUpdates()
            } else {
                stopProgressUpdates()
            }
        }
        
        override fun onPlaybackStateChanged(playbackState: Int) {
            when (playbackState) {
                Player.STATE_READY -> {
                    val controller = mediaController ?: return
                    _uiState.update { 
                        it.copy(duration = controller.duration)
                    }
                    if (!audioEffectsInitialized && controller.audioSessionId != 0) {
                        audioEffectsManager.initialize(controller.audioSessionId)
                        audioEffectsInitialized = true
                    }
                }
                Player.STATE_ENDED -> {
                    handleSongEnded()
                }
            }
        }
        
        override fun onMediaItemTransition(
            mediaItem: MediaItem?,
            reason: Int
        ) {
            updateCurrentPosition()
        }
    }
    
    init {
        initializeAudioEngine()
        loadSongs()
        loadPlaylists()
        loadAlbums()
        connectToService()
        observeCurrentSong()
    }
    
    private fun observeCurrentSong() {
        viewModelScope.launch {
            uiState.map { it.currentSong }
                .distinctUntilChanged()
                .collect { song ->
                    if (song != null) {
                        loadLyrics(song)
                    } else {
                        _uiState.update { it.copy(lyrics = null, currentLyricIndex = -1) }
                    }
                }
        }
    }
    
    private fun loadLyrics(song: Song) {
        viewModelScope.launch {
            // Reset lyrics first
            _uiState.update { it.copy(lyrics = null, currentLyricIndex = -1) }
            repository.getLyrics(song).collect { lyrics ->
                _uiState.update { it.copy(lyrics = lyrics) }
            }
        }
    }
    
    private fun initializeAudioEngine() {
        try {
            _audioEngine = AudioEngine.getInstance().apply {
                create()
            }
            android.util.Log.i("MusicViewModel", "AudioEngine singleton obtained for effects control")
        } catch (e: Exception) {
            android.util.Log.e("MusicViewModel", "Failed to get AudioEngine", e)
        }
    }
    
    private fun connectToService() {
        startService()
        
        val app = getApplication<Application>()
        val sessionToken = SessionToken(
            app,
            ComponentName(app, MusicPlaybackService::class.java)
        )
        controllerFuture = MediaController.Builder(app, sessionToken).buildAsync()
        controllerFuture?.addListener({
            try {
                mediaController = controllerFuture?.get()
                mediaController?.addListener(playerListener)
                // Sync current playback state from service
                syncWithController()
            } catch (e: Exception) {
                android.util.Log.e("MusicViewModel", "Failed to connect to service", e)
            }
        }, MoreExecutors.directExecutor())
    }
    
    private fun startService() {
        val app = getApplication<Application>()
        val intent = Intent(app, MusicPlaybackService::class.java)
        app.startService(intent)
    }
    
    /**
     * Sync UI state with the current playback state from MediaController.
     * This is called when the controller connects to restore the playback state
     * when opening the app from widget or notification.
     */
    private fun syncWithController() {
        val controller = mediaController ?: return
        
        viewModelScope.launch {
            try {
                val currentMediaItem = controller.currentMediaItem
                if (currentMediaItem != null) {
                    // Get song info from media item metadata
                    val title = currentMediaItem.mediaMetadata.title?.toString() ?: ""
                    val artist = currentMediaItem.mediaMetadata.artist?.toString() ?: ""
                    
                    // Try to find the song in the database by matching title and artist
                    val songs = _uiState.value.songs
                    var matchingSong = songs.find { 
                        it.title == title && it.artist == artist 
                    }
                    
                    // If songs not loaded yet, wait and try again
                    if (matchingSong == null && songs.isEmpty()) {
                        delay(500) // Wait for songs to load
                        matchingSong = _uiState.value.songs.find { 
                            it.title == title && it.artist == artist 
                        }
                    }
                    
                    if (matchingSong != null) {
                        val currentPosition = controller.currentPosition
                        val duration = controller.duration.takeIf { it > 0 } ?: matchingSong.duration
                        val progress = if (duration > 0) currentPosition.toFloat() / duration else 0f
                        
                        _uiState.update {
                            it.copy(
                                currentSong = matchingSong,
                                isPlaying = controller.isPlaying,
                                currentPosition = currentPosition,
                                duration = duration,
                                progress = progress.coerceIn(0f, 1f)
                            )
                        }
                        
                        android.util.Log.i("MusicViewModel", "Synced with controller: ${matchingSong.title}, playing: ${controller.isPlaying}")
                        
                        // Start progress updates if playing
                        if (controller.isPlaying) {
                            startProgressUpdates()
                        }
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("MusicViewModel", "Failed to sync with controller", e)
            }
        }
    }
    
    private fun loadSongs() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            repository.getAllSongs().collect { songs ->
                _uiState.update { 
                    it.copy(
                        songs = songs,
                        isLoading = false
                    )
                }
            }
        }
    }
    
    private fun loadPlaylists() {
        viewModelScope.launch {
            repository.getAllPlaylists().collect { playlists ->
                _uiState.update { it.copy(playlists = playlists) }
            }
        }
    }
    
    fun scanMusic() {
        viewModelScope.launch {
            _uiState.update { it.copy(isScanning = true, error = null) }
            try {
                val count = repository.scanAndImportMusic()
                _uiState.update { 
                    it.copy(
                        isScanning = false,
                        error = if (count == 0) "No music found on device" else null
                    )
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isScanning = false,
                        error = "Failed to scan music: ${e.message}"
                    )
                }
            }
        }
    }
    
    fun refreshLibrary() {
        viewModelScope.launch {
            _uiState.update { it.copy(isScanning = true, error = null) }
            try {
                val count = repository.refreshLibrary()
                _uiState.update { 
                    it.copy(
                        isScanning = false,
                        error = if (count == 0) "No music found on device" else null
                    )
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isScanning = false,
                        error = "Failed to refresh library: ${e.message}"
                    )
                }
            }
        }
    }
    
    fun searchSongs(query: String) {
        _searchQuery.value = query
        _uiState.update { it.copy(searchQuery = query) }
        
        if (query.isEmpty()) {
            loadSongs()
        } else {
            viewModelScope.launch {
                repository.searchSongs(query).collect { songs ->
                    _uiState.update { it.copy(songs = songs) }
                }
            }
        }
    }
    
    fun playSong(song: Song) {
        val controller = mediaController ?: return
        
        val currentState = _uiState.value
        val songs = currentState.songs
        val songIndex = songs.indexOfFirst { it.id == song.id }
        
        _uiState.update { 
            it.copy(
                queue = songs,
                currentQueueIndex = songIndex,
                currentSong = song,
                isPlaying = true,
                progress = 0f,
                currentPosition = 0L,
                duration = song.duration
            )
        }
        
        val contentUri = ContentUris.withAppendedId(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            song.id
        )
        
        val mediaItem = MediaItem.Builder()
            .setUri(contentUri)
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setTitle(song.title)
                    .setArtist(song.artist)
                    .setAlbumTitle(song.album)
                    .setArtworkUri(song.albumArtUri?.let { Uri.parse(it) })
                    .build()
            )
            .build()
        
        controller.setMediaItem(mediaItem)
        controller.prepare()
        controller.play()
        
        // Save queue for widget navigation
        saveQueueForWidget(songs, songIndex)
    }
    
    fun playSongFromList(song: Song, songList: List<Song>) {
        val controller = mediaController ?: return
        
        val songIndex = songList.indexOfFirst { it.id == song.id }
        
        _uiState.update { 
            it.copy(
                queue = songList,
                currentQueueIndex = if (songIndex >= 0) songIndex else 0,
                currentSong = song,
                isPlaying = true,
                progress = 0f,
                currentPosition = 0L,
                duration = song.duration
            )
        }
        
        val contentUri = ContentUris.withAppendedId(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            song.id
        )
        
        val mediaItem = MediaItem.Builder()
            .setUri(contentUri)
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setTitle(song.title)
                    .setArtist(song.artist)
                    .setAlbumTitle(song.album)
                    .setArtworkUri(song.albumArtUri?.let { Uri.parse(it) })
                    .build()
            )
            .build()
        
        controller.setMediaItem(mediaItem)
        controller.prepare()
        controller.play()
        
        // Save queue for widget navigation
        saveQueueForWidget(songList, if (songIndex >= 0) songIndex else 0)
    }
    
    fun togglePlayPause() {
        val controller = mediaController ?: return
        if (controller.isPlaying) {
            controller.pause()
        } else {
            controller.play()
        }
    }

    fun setPlaybackParameters(tempo: Float, pitch: Float) {
        try {
            mediaController?.let { controller ->
                // Pitch in ExoPlayer is a factor (1.0 = normal), but our UI sends semitones (-12 to +12).
                // Convert semitones to factor: factor = 2^(semitones/12)
                val pitchFactor = java.lang.Math.pow(2.0, pitch.toDouble() / 12.0).toFloat()
                
                // Ensure tempo and pitch are positive to avoid IllegalArgumentException
                val safeTempo = tempo.coerceAtLeast(0.1f)
                val safePitch = pitchFactor.coerceAtLeast(0.1f)
                
                val params = androidx.media3.common.PlaybackParameters(safeTempo, safePitch)
                controller.playbackParameters = params
                
                // Update UI state with the original semitone value for sliders
                _uiState.update { it.copy(tempo = tempo, pitch = pitch) }
            }
        } catch (e: Exception) {
            android.util.Log.e("MusicViewModel", "Failed to set playback parameters", e)
        }
    }
    
    fun playNext() {
        val controller = mediaController ?: return
        val currentState = _uiState.value
        val queue = currentState.queue.ifEmpty { currentState.songs }
        
        if (queue.isEmpty()) return
        
        val currentIndex = currentState.currentQueueIndex
        val nextIndex = if (currentState.isShuffleOn) {
            (queue.indices).random()
        } else {
            (currentIndex + 1) % queue.size
        }
        
        val nextSong = queue[nextIndex]
        _uiState.update { 
            it.copy(
                currentQueueIndex = nextIndex,
                currentSong = nextSong,
                progress = 0f,
                currentPosition = 0L,
                duration = nextSong.duration
            )
        }
        
        val contentUri = ContentUris.withAppendedId(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            nextSong.id
        )
        
        val mediaItem = MediaItem.Builder()
            .setUri(contentUri)
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setTitle(nextSong.title)
                    .setArtist(nextSong.artist)
                    .setAlbumTitle(nextSong.album)
                    .setArtworkUri(nextSong.albumArtUri?.let { Uri.parse(it) })
                    .build()
            )
            .build()
        
        controller.setMediaItem(mediaItem)
        controller.prepare()
        controller.play()
    }
    
    fun playPrevious() {
        val controller = mediaController ?: return
        val currentState = _uiState.value
        val queue = currentState.queue.ifEmpty { currentState.songs }
        
        if (queue.isEmpty()) return
        
        if (controller.currentPosition > 3000) {
            controller.seekTo(0)
            return
        }
        
        val currentIndex = currentState.currentQueueIndex
        val prevIndex = if (currentIndex > 0) currentIndex - 1 else queue.size - 1
        
        val prevSong = queue[prevIndex]
        _uiState.update { 
            it.copy(
                currentQueueIndex = prevIndex,
                currentSong = prevSong,
                progress = 0f,
                currentPosition = 0L,
                duration = prevSong.duration
            )
        }
        
        val contentUri = ContentUris.withAppendedId(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            prevSong.id
        )
        
        val mediaItem = MediaItem.Builder()
            .setUri(contentUri)
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setTitle(prevSong.title)
                    .setArtist(prevSong.artist)
                    .setAlbumTitle(prevSong.album)
                    .setArtworkUri(prevSong.albumArtUri?.let { Uri.parse(it) })
                    .build()
            )
            .build()
        
        controller.setMediaItem(mediaItem)
        controller.prepare()
        controller.play()
    }
    
    fun seekTo(progress: Float) {
        val controller = mediaController ?: return
        val duration = _uiState.value.duration
        val position = (progress * duration).toLong()
        controller.seekTo(position)
        _uiState.update { 
            it.copy(
                progress = progress,
                currentPosition = position
            )
        }
    }
    
    fun seekToPosition(positionMs: Long) {
        val controller = mediaController ?: return
        controller.seekTo(positionMs)
        val duration = _uiState.value.duration
        val progress = if (duration > 0) positionMs.toFloat() / duration else 0f
        _uiState.update { 
            it.copy(
                currentPosition = positionMs,
                progress = progress
            )
        }
    }
    
    fun toggleShuffle() {
        val controller = mediaController ?: return
        val newShuffleState = !_uiState.value.isShuffleOn
        _uiState.update { it.copy(isShuffleOn = newShuffleState) }
        controller.shuffleModeEnabled = newShuffleState
    }
    
    fun toggleRepeat() {
        val controller = mediaController ?: return
        val newRepeatMode = (_uiState.value.repeatMode + 1) % 3
        _uiState.update { it.copy(repeatMode = newRepeatMode) }
        
        controller.repeatMode = when (newRepeatMode) {
            1 -> Player.REPEAT_MODE_ALL
            2 -> Player.REPEAT_MODE_ONE
            else -> Player.REPEAT_MODE_OFF
        }
    }
    
    private fun startProgressUpdates() {
        stopProgressUpdates()
        progressJob = viewModelScope.launch {
            while (isActive) {
                updateCurrentPosition()
                delay(500L)
            }
        }
    }
    
    private fun stopProgressUpdates() {
        progressJob?.cancel()
        progressJob = null
    }
    
    private fun updateCurrentPosition() {
        val controller = mediaController ?: return
        val currentPosition = controller.currentPosition
        val duration = controller.duration.takeIf { it > 0 } ?: _uiState.value.duration
        val progress = if (duration > 0) currentPosition.toFloat() / duration else 0f
        
        val lyrics = _uiState.value.lyrics
        val currentLyricIndex = if (lyrics != null && lyrics.isSynced) {
            lyrics.lines.indexOfLast { it.timestamp <= currentPosition }
        } else {
            -1
        }
        
        _uiState.update {
            it.copy(
                currentPosition = currentPosition,
                duration = duration,
                progress = progress.coerceIn(0f, 1f),
                currentLyricIndex = currentLyricIndex
            )
        }
    }
    
    private fun handleSongEnded() {
        val currentState = _uiState.value
        val queue = currentState.queue.ifEmpty { currentState.songs }
        
        when (currentState.repeatMode) {
            2 -> {
                mediaController?.seekTo(0)
                mediaController?.play()
            }
            1 -> {
                playNext()
            }
            else -> {
                val currentIndex = currentState.currentQueueIndex
                if (currentIndex < queue.size - 1) {
                    playNext()
                } else {
                    _uiState.update { it.copy(isPlaying = false, progress = 0f) }
                }
            }
        }
    }
    
    fun createPlaylist(name: String) {
        viewModelScope.launch {
            repository.createPlaylist(name)
        }
    }
    
    fun deletePlaylist(playlist: Playlist) {
        viewModelScope.launch {
            repository.deletePlaylist(playlist)
        }
    }
    
    fun addSongToPlaylist(playlistId: Long, songId: Long) {
        viewModelScope.launch {
            repository.addSongToPlaylist(playlistId, songId)
        }
    }
    
    fun loadPlaylistSongs(playlistId: Long) {
        viewModelScope.launch {
            repository.getSongsInPlaylist(playlistId).collect { songs ->
                _uiState.update { it.copy(playlistSongs = songs) }
            }
        }
    }
    
    fun createPlaylistFromAlbum(album: com.oss.euphoriae.data.model.Album) {
        viewModelScope.launch {
            try {
                // 1. Create playlist with album name (Artist - Album)
                val playlistName = "${album.artist} - ${album.name}"
                val playlistId = repository.createPlaylist(name = playlistName)
                
                // 2. Get all songs from this album (by name, to catch merged albums)
                val albumSongs = repository.getSongsByAlbumName(album.name)
                
                // 3. Add all songs to the new playlist
                albumSongs.forEach { song ->
                    repository.addSongToPlaylist(playlistId, song.id)
                }
                
                // 4. Force refresh of playlists to show the new one immediately
                // The repository.getAllPlaylists() flow should handle this automatically, 
                // but sometimes it needs a nudge if we just inserted.
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(error = "Failed to create playlist from album: ${e.message}")
                }
            }
        }
    }
    
    private fun loadAlbums() {
        viewModelScope.launch {
            repository.getAlbums().collect { albums ->
                _uiState.update { it.copy(albums = albums) }
            }
        }
    }
    
    private fun saveQueueForWidget(songs: List<Song>, currentIndex: Int) {
        viewModelScope.launch {
            val queueInfo = songs.map { song ->
                QueueSongInfo(
                    id = song.id,
                    title = song.title,
                    artist = song.artist,
                    album = song.album,
                    albumArtUri = song.albumArtUri,
                    duration = song.duration
                )
            }
            WidgetQueueManager.saveQueue(getApplication(), queueInfo, currentIndex)
        }
    }
    
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
    
    override fun onCleared() {
        super.onCleared()
        stopProgressUpdates()
        mediaController?.removeListener(playerListener)
        audioEffectsManager.release()
        // Note: Don't destroy audioEngine here - it's a singleton managed by the service
        _audioEngine = null
        controllerFuture?.let { MediaController.releaseFuture(it) }
    }
}
