package com.oss.euphoriae.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.compose.ui.layout.ContentScale
import coil.compose.SubcomposeAsyncImage
import com.oss.euphoriae.data.model.Playlist
import com.oss.euphoriae.data.model.Song
import com.oss.euphoriae.ui.components.AlbumCardCompact
import com.oss.euphoriae.ui.components.PlaylistCardCompact
import com.oss.euphoriae.ui.components.SongListItem
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    songs: List<Song>,
    playlists: List<Playlist>,
    isScanning: Boolean,
    onSongClick: (Song, List<Song>) -> Unit,
    onScanClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    
    // Permission handling for audio
    val audioPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        Manifest.permission.READ_MEDIA_AUDIO
    } else {
        Manifest.permission.READ_EXTERNAL_STORAGE
    }
    
    var hasPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, audioPermission) == PackageManager.PERMISSION_GRANTED
        )
    }
    
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasPermission = isGranted
        if (isGranted) {
            onScanClick()
        }
    }
    
    // Request notification permission on Android 13+
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { _ -> }
    
    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val hasNotificationPermission = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            
            if (!hasNotificationPermission) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
    
    Scaffold(
        modifier = modifier,
        topBar = {
            LargeTopAppBar(
                title = {
                        Text(
                            text = "Euphoriae",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold
                        )
                },

                actions = {
                    if (isScanning) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                    } else {
                        IconButton(onClick = {
                            if (hasPermission) {
                                onScanClick()
                            } else {
                            permissionLauncher.launch(audioPermission)
                            }
                        }) {
                            Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                        }
                    }
                    IconButton(onClick = { }) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                },
                colors = TopAppBarDefaults.largeTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        if (songs.isEmpty() && !isScanning) {
            // Empty state - prompt to scan
            EmptyMusicState(
                hasPermission = hasPermission,
                onScanClick = {
                    if (hasPermission) {
                        onScanClick()
                    } else {
                        permissionLauncher.launch(audioPermission)
                    }
                },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(bottom = 100.dp)
            ) {
                // Recently Added Section
                if (songs.isNotEmpty()) {
                    item {
                        SectionHeader(title = "Recently Added")
                    }
                    
                    item {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            contentPadding = PaddingValues(horizontal = 16.dp)
                        ) {
                            val recentlyAdded = songs.take(6)
                            items(
                                items = recentlyAdded,
                                key = { song -> song.id }
                            ) { song ->
                                SongCard(
                                    song = song,
                                    onClick = { onSongClick(song, recentlyAdded) }
                                )
                            }
                        }
                    }
                    
                    item { Spacer(modifier = Modifier.height(24.dp)) }
                }
                
                // Your Playlists
                if (playlists.isNotEmpty()) {
                    item {
                        SectionHeader(title = "Your Playlists")
                    }
                    
                    item {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            contentPadding = PaddingValues(horizontal = 16.dp)
                        ) {
                            items(playlists) { playlist ->
                                PlaylistCardCompact(
                                    playlist = playlist,
                                    onClick = { }
                                )
                            }
                        }
                    }
                    
                    item { Spacer(modifier = Modifier.height(24.dp)) }
                }
                
                // All Songs
                if (songs.isNotEmpty()) {
                    item {
                        SectionHeader(title = "All Songs (${songs.size})")
                    }
                    
                    val allSongsPreview = songs.take(10)
                    items(
                        items = allSongsPreview,
                        key = { song -> song.id }
                    ) { song ->
                        SongListItem(
                            song = song,
                            onClick = { onSongClick(song, allSongsPreview) },
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyMusicState(
    hasPermission: Boolean,
    onScanClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.MusicNote,
            contentDescription = null,
            modifier = Modifier.size(120.dp),
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = "No Music Found",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = if (hasPermission) {
                "Tap the button below to scan your device for music"
            } else {
                "Grant permission to access your music library"
            },
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        FilledTonalButton(
            onClick = onScanClick,
            contentPadding = PaddingValues(horizontal = 32.dp, vertical = 16.dp)
        ) {
            Icon(
                imageVector = if (hasPermission) Icons.Default.LibraryMusic else Icons.Default.FolderOpen,
                contentDescription = null
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (hasPermission) "Scan Music" else "Grant Permission",
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}

@Composable
private fun SongCard(
    song: Song,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        onClick = onClick,
        modifier = modifier.width(150.dp),
        shape = MaterialTheme.shapes.large
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            // Album Art Placeholder
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f),
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.secondaryContainer
            ) {
                Box(contentAlignment = Alignment.Center) {
                    SubcomposeAsyncImage(
                        model = song.albumArtUri,
                        contentDescription = "Album Art",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                        loading = {
                            Icon(
                                imageVector = Icons.Default.MusicNote,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp),
                                tint = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        },
                        error = {
                            Icon(
                                imageVector = Icons.Default.MusicNote,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp),
                                tint = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = song.title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium,
                maxLines = 1
            )
            
            Text(
                text = song.artist,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold,
        modifier = modifier.padding(horizontal = 16.dp, vertical = 12.dp)
    )
}

