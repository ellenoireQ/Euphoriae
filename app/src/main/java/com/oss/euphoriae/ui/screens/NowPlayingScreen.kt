package com.oss.euphoriae.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import androidx.palette.graphics.Palette
import coil.compose.AsyncImagePainter
import coil.compose.SubcomposeAsyncImage
import coil.compose.SubcomposeAsyncImageContent
import coil.request.ImageRequest
import com.oss.euphoriae.data.model.Playlist
import com.oss.euphoriae.data.model.Song
import com.oss.euphoriae.ui.components.MusicVisualizer
import com.oss.euphoriae.ui.theme.EuphoriaeTheme



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NowPlayingScreen(
    song: Song,
    isPlaying: Boolean,
    progress: Float,
    onBackClick: () -> Unit,
    onPlayPauseClick: () -> Unit,
    onPreviousClick: () -> Unit,
    onNextClick: () -> Unit,
    onShuffleClick: () -> Unit,
    onRepeatClick: () -> Unit,
    onProgressChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    isShuffleOn: Boolean = false,
    repeatMode: Int = 0, // 0 = off, 1 = all, 2 = one
    playlists: List<Playlist> = emptyList(),
    onAddToPlaylist: (Long) -> Unit = {} // playlistId
) {
    
    var showPlaylistDialog by remember { mutableStateOf(false) }
    
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        label = "progress"
    )
    
    
    var hasAlbumArt by remember { mutableStateOf(false) }
    var dominantColor by remember { mutableStateOf(Color.Transparent) }
    
    
    LaunchedEffect(song.id) {
        hasAlbumArt = false
        dominantColor = Color.Transparent
    }
    
    
    val accentColor = if (hasAlbumArt) dominantColor else MaterialTheme.colorScheme.primary
    
    
    val backgroundBrush = if (hasAlbumArt) {
        androidx.compose.ui.graphics.Brush.verticalGradient(
            colors = listOf(
                dominantColor.copy(alpha = 0.6f),
                MaterialTheme.colorScheme.surface
            )
        )
    } else {
        androidx.compose.ui.graphics.Brush.verticalGradient(
            colors = listOf(
                MaterialTheme.colorScheme.surface,
                MaterialTheme.colorScheme.surface
            )
        )
    }
    
    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.surface,
        topBar = {
            TopAppBar(
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Back",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Default.ArrowBackIosNew,
                            contentDescription = "Close"
                        )
                    }
                },
                actions = {
                    
                    IconButton(onClick = { showPlaylistDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.PlaylistAdd,
                            contentDescription = "Add to Playlist"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(brush = backgroundBrush)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(innerPadding)
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(24.dp))
                
                
                SubcomposeAsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(song.albumArtUri)
                        .crossfade(true)
                        .allowHardware(false) // Required for Palette to work
                        .build(),
                    contentDescription = "Album Art",
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(24.dp)),
                    contentScale = ContentScale.Crop,
                    loading = {
                        MusicVisualizer(
                            isPlaying = isPlaying,
                            modifier = Modifier.fillMaxSize()
                        )
                    },
                    error = {
                        MusicVisualizer(
                            isPlaying = isPlaying,
                            modifier = Modifier.fillMaxSize()
                        )
                    },
                    success = { state ->
                        
                        hasAlbumArt = true
                        
                        LaunchedEffect(state.result.drawable) {
                            val bitmap = state.result.drawable.toBitmap()
                            Palette.from(bitmap).generate { palette ->
                                palette?.let { p ->
                                    val color = p.vibrantSwatch?.rgb
                                        ?: p.dominantSwatch?.rgb
                                        ?: p.mutedSwatch?.rgb
                                    color?.let { dominantColor = Color(it) }
                                }
                            }
                        }
                        SubcomposeAsyncImageContent()
                    }
                )
                
                Spacer(modifier = Modifier.height(32.dp))
            
                
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = song.title,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = song.artist,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                    
                    Text(
                        text = song.album,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.outline,
                        textAlign = TextAlign.Center
                    )
                }
                
                Spacer(modifier = Modifier.height(32.dp))
                
                
                song.getFormatName()?.let { formatName ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (song.isLossless()) {
                            
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = accentColor.copy(alpha = 0.15f),
                                border = BorderStroke(
                                    1.dp, 
                                    accentColor.copy(alpha = 0.5f)
                                )
                            ) {
                                Text(
                                    text = "LOSSLESS",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = accentColor,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
                
                
                Column(modifier = Modifier.fillMaxWidth()) {
                    Slider(
                        value = animatedProgress,
                        onValueChange = onProgressChange,
                        modifier = Modifier.fillMaxWidth(),
                        colors = SliderDefaults.colors(
                            thumbColor = accentColor,
                            activeTrackColor = accentColor,
                            inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = formatTime((progress * song.duration).toLong()),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = song.formatDuration(),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Shuffle
                    IconButton(onClick = onShuffleClick) {
                        Icon(
                            imageVector = Icons.Default.Shuffle,
                            contentDescription = "Shuffle",
                            tint = if (isShuffleOn) accentColor 
                                   else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    
                    FilledTonalIconButton(
                        onClick = onPreviousClick,
                        modifier = Modifier.size(56.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.SkipPrevious,
                            contentDescription = "Previous",
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    
                    
                    FilledIconButton(
                        onClick = onPlayPauseClick,
                        modifier = Modifier.size(80.dp),
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = accentColor
                        ),
                        shape = RoundedCornerShape(30.dp)
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (isPlaying) "Pause" else "Play",
                            modifier = Modifier.size(36.dp)
                        )
                    }
                    
                    
                    FilledTonalIconButton(
                        onClick = onNextClick,
                        modifier = Modifier.size(56.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.SkipNext,
                            contentDescription = "Next",
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    
                    
                    IconButton(onClick = onRepeatClick) {
                        Icon(
                            imageVector = when (repeatMode) {
                                2 -> Icons.Default.RepeatOne
                                else -> Icons.Default.Repeat
                            },
                            contentDescription = "Repeat",
                            tint = if (repeatMode > 0) accentColor
                                   else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
    
    
    if (showPlaylistDialog) {
        AlertDialog(
            onDismissRequest = { showPlaylistDialog = false },
            title = { 
                Text(
                    text = "Add to Playlist",
                    fontWeight = FontWeight.SemiBold
                ) 
            },
            text = {
                if (playlists.isEmpty()) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlaylistPlay,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No playlists yet",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Create a playlist first",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                } else {
                    LazyColumn {
                        items(playlists) { playlist ->
                            ListItem(
                                headlineContent = { 
                                    Text(
                                        text = playlist.name,
                                        fontWeight = FontWeight.Medium
                                    )
                                },
                                leadingContent = {
                                    Icon(
                                        imageVector = Icons.Default.PlaylistPlay,
                                        contentDescription = null
                                    )
                                },
                                modifier = Modifier.clickable {
                                    onAddToPlaylist(playlist.id)
                                    showPlaylistDialog = false
                                }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showPlaylistDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

private fun formatTime(millis: Long): String {
    val minutes = (millis / 1000) / 60
    val seconds = (millis / 1000) % 60
    return String.format("%d:%02d", minutes, seconds)
}

@Preview(showBackground = true)
@Composable
fun NowPlayingScreenPreview() {
    EuphoriaeTheme {
        NowPlayingScreen(
            song = Song(
                id = 1,
                title = "Blinding Lights",
                artist = "The Weeknd",
                album = "After Hours",
                duration = 200000
            ),
            isPlaying = true,
            progress = 0.4f,
            onBackClick = {},
            onPlayPauseClick = {},
            onPreviousClick = {},
            onNextClick = {},
            onShuffleClick = {},
            onRepeatClick = {},
            onProgressChange = {}
        )
    }
}
