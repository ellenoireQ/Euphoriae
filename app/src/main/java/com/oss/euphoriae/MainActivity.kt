package com.oss.euphoriae

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlaylistPlay
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.oss.euphoriae.data.model.Song
import com.oss.euphoriae.data.preferences.ThemeColorOption
import com.oss.euphoriae.data.preferences.ThemePreferences
import com.oss.euphoriae.ui.components.MiniPlayer
import com.oss.euphoriae.ui.screens.EqualizerScreen
import com.oss.euphoriae.ui.screens.HomeScreen
import com.oss.euphoriae.ui.screens.NowPlayingScreen
import com.oss.euphoriae.ui.screens.PlaylistDetailScreen
import com.oss.euphoriae.ui.screens.PlaylistScreen
import com.oss.euphoriae.ui.screens.SettingsScreen
import com.oss.euphoriae.ui.screens.SongsScreen
import com.oss.euphoriae.ui.theme.EuphoriaeTheme
import com.oss.euphoriae.ui.viewmodel.MusicViewModel
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    
    private val themePreferences by lazy { ThemePreferences(applicationContext) }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val themeColor by themePreferences.themeColor.collectAsStateWithLifecycle(
                initialValue = ThemeColorOption.DYNAMIC
            )
            
            EuphoriaeTheme(
                darkTheme = true,
                themeColor = themeColor
            ) {
                EuphoriaeMainApp(
                    themePreferences = themePreferences,
                    currentThemeColor = themeColor
                )
            }
        }
    }
}

enum class Destination(
    val route: String,
    val label: String,
    val icon: ImageVector,
    val contentDescription: String
) {
    HOME("home", "Home", Icons.Default.Home, "Home"),
    SONGS("songs", "Songs", Icons.Default.MusicNote, "Songs"),
    PLAYLISTS("playlists", "Playlists", Icons.Default.PlaylistPlay, "Playlists"),
    EQUALIZER("equalizer", "Equalizer", Icons.Default.GraphicEq, "Equalizer")
}

@Composable
fun EuphoriaeMainApp(
    viewModel: MusicViewModel = viewModel(),
    themePreferences: ThemePreferences,
    currentThemeColor: ThemeColorOption
) {
    val navController = rememberNavController()
    val startDestination = Destination.HOME
    var selectedDestination by rememberSaveable { mutableIntStateOf(startDestination.ordinal) }
    
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    var showNowPlaying by remember { mutableStateOf(false) }
    
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    
    val showBottomBar = currentRoute != "now_playing"
    
    val snackbarHostState = remember { SnackbarHostState() }
    
    val coroutineScope = rememberCoroutineScope()
    
    LaunchedEffect(uiState.error) {
        uiState.error?.let { error ->
            snackbarHostState.showSnackbar(error)
            viewModel.clearError()
        }
    }
    
    if (showNowPlaying && uiState.currentSong != null) {
        NowPlayingScreen(
            song = uiState.currentSong!!,
            isPlaying = uiState.isPlaying,
            progress = uiState.progress,
            isShuffleOn = uiState.isShuffleOn,
            repeatMode = uiState.repeatMode,
            playlists = uiState.playlists,
            onBackClick = { showNowPlaying = false },
            onPlayPauseClick = { viewModel.togglePlayPause() },
            onPreviousClick = { viewModel.playPrevious() },
            onNextClick = { viewModel.playNext() },
            onShuffleClick = { viewModel.toggleShuffle() },
            onRepeatClick = { viewModel.toggleRepeat() },
            onProgressChange = { viewModel.seekTo(it) },
            onAddToPlaylist = { playlistId -> 
                uiState.currentSong?.let { song ->
                    viewModel.addSongToPlaylist(playlistId, song.id)
                }
            }
        )
    } else {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            snackbarHost = { SnackbarHost(snackbarHostState) },
            bottomBar = {
                Column {
                    AnimatedVisibility(
                        visible = uiState.currentSong != null && showBottomBar,
                        enter = slideInVertically(initialOffsetY = { it }),
                        exit = slideOutVertically(targetOffsetY = { it })
                    ) {
                        MiniPlayer(
                            currentSong = uiState.currentSong,
                            isPlaying = uiState.isPlaying,
                            progress = uiState.progress,
                            onPlayPauseClick = { viewModel.togglePlayPause() },
                            onSkipNextClick = { viewModel.playNext() },
                            onClick = { showNowPlaying = true }
                        )
                    }
                    
                    if (showBottomBar) {
                        NavigationBar(
                            modifier = Modifier
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                                .clip(RoundedCornerShape(24.dp)),
                            tonalElevation = 0.dp
                        ) {
                            Destination.entries.forEachIndexed { index, destination ->
                                NavigationBarItem(
                                    selected = selectedDestination == index,
                                    onClick = {
                                        navController.navigate(destination.route) {
                                            popUpTo(navController.graph.startDestinationId) {
                                                saveState = true
                                            }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                        selectedDestination = index
                                    },
                                    icon = {
                                        Icon(
                                            destination.icon,
                                            contentDescription = destination.contentDescription
                                        )
                                    },
                                    label = { Text(destination.label) }
                                )
                            }
                        }
                    }
                }
            }
        ) { contentPadding ->
            AppNavHost(
                navController = navController,
                startDestination = startDestination,
                uiState = uiState,
                onSongClick = { song, list -> viewModel.playSongFromList(song, list) },
                onScanClick = { viewModel.scanMusic() },
                onSearchQueryChange = { viewModel.searchSongs(it) },
                onCreatePlaylist = { viewModel.createPlaylist(it) },
                onDeletePlaylist = { viewModel.deletePlaylist(it) },
                onLoadPlaylistSongs = { viewModel.loadPlaylistSongs(it) },
                playlistSongs = uiState.playlistSongs,
                audioEffectsManager = viewModel.audioEffectsManager,
                currentThemeColor = currentThemeColor,
                onThemeColorChange = { option ->
                    coroutineScope.launch {
                        themePreferences.setThemeColor(option)
                    }
                },
                modifier = Modifier.padding(contentPadding)
            )
        }
    }
}

@Composable
fun AppNavHost(
    navController: NavHostController,
    startDestination: Destination,
    uiState: com.oss.euphoriae.ui.viewmodel.MusicUiState,
    onSongClick: (Song, List<Song>) -> Unit,
    onScanClick: () -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onCreatePlaylist: (String) -> Unit,
    onDeletePlaylist: (com.oss.euphoriae.data.model.Playlist) -> Unit,
    onLoadPlaylistSongs: (Long) -> Unit,
    playlistSongs: List<Song>,
    audioEffectsManager: com.oss.euphoriae.data.`class`.AudioEffectsManager,
    currentThemeColor: ThemeColorOption,
    onThemeColorChange: (ThemeColorOption) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedPlaylist by remember { mutableStateOf<com.oss.euphoriae.data.model.Playlist?>(null) }
    
    NavHost(
        navController = navController,
        startDestination = startDestination.route,
        modifier = modifier
    ) {
        composable(Destination.HOME.route) {
            HomeScreen(
                songs = uiState.songs,
                playlists = uiState.playlists,
                isScanning = uiState.isScanning,
                onSongClick = onSongClick,
                onScanClick = onScanClick,
                onSettingsClick = { navController.navigate("settings") }
            )
        }
        composable(Destination.SONGS.route) {
            SongsScreen(
                songs = uiState.songs,
                searchQuery = uiState.searchQuery,
                onSearchQueryChange = onSearchQueryChange,
                onSongClick = onSongClick,
                currentPlayingSong = uiState.currentSong
            )
        }
        composable(Destination.PLAYLISTS.route) {
            PlaylistScreen(
                playlists = uiState.playlists,
                onPlaylistClick = { playlist ->
                    selectedPlaylist = playlist
                    onLoadPlaylistSongs(playlist.id)
                    navController.navigate("playlist_detail")
                },
                onCreatePlaylist = onCreatePlaylist
            )
        }
        composable("playlist_detail") {
            selectedPlaylist?.let { playlist ->
                PlaylistDetailScreen(
                    playlist = playlist,
                    songs = playlistSongs,
                    currentPlayingSong = uiState.currentSong,
                    onBackClick = { navController.popBackStack() },
                    onSongClick = onSongClick,
                    onDeletePlaylist = {
                        onDeletePlaylist(playlist)
                        navController.popBackStack()
                    }
                )
            }
        }
        composable(Destination.EQUALIZER.route) {
            EqualizerScreen(audioEffectsManager = audioEffectsManager)
        }
        composable("settings") {
            SettingsScreen(
                onBackClick = { navController.popBackStack() },
                currentThemeColor = currentThemeColor,
                onThemeColorChange = onThemeColorChange
            )
        }
    }
}
