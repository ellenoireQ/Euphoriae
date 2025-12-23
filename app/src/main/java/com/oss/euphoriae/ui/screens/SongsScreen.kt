package com.oss.euphoriae.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SortByAlpha
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.oss.euphoriae.data.model.Song
import com.oss.euphoriae.ui.components.SongListItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SongsScreen(
    songs: List<Song>,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onSongClick: (Song, List<Song>) -> Unit,
    currentPlayingSong: Song? = null,
    modifier: Modifier = Modifier
) {
    var isSearchActive by remember { mutableStateOf(false) }
    
    Scaffold(
        modifier = modifier,
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        Text(
                            text = "Songs",
                            fontWeight = FontWeight.Bold
                        )
                    },
                    actions = {
                        IconButton(onClick = { isSearchActive = !isSearchActive }) {
                            Icon(Icons.Default.Search, contentDescription = "Search")
                        }
                        IconButton(onClick = { }) {
                            Icon(Icons.Default.SortByAlpha, contentDescription = "Sort")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )
                
                
                if (isSearchActive) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = onSearchQueryChange,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        placeholder = { Text("Search songs...") },
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = null)
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { onSearchQueryChange("") }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear")
                                }
                            }
                        },
                        singleLine = true,
                        shape = MaterialTheme.shapes.large
                    )
                }
            }
        }
    ) { innerPadding ->
        if (songs.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = if (searchQuery.isEmpty()) "No songs yet" else "No songs found",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = if (searchQuery.isEmpty()) {
                        "Scan your device for music from the Home screen"
                    } else {
                        "Try a different search term"
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                
                item {
                    FilledTonalButton(
                        onClick = { 
                            if (songs.isNotEmpty()) {
                                onSongClick(songs.random(), songs)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        contentPadding = PaddingValues(16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shuffle,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Shuffle All (${songs.size} songs)",
                            style = MaterialTheme.typography.titleSmall
                        )
                    }
                }
                
                
                item {
                    Text(
                        text = "${songs.size} songs",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
                
                
                items(songs, key = { it.id }) { song ->
                    SongListItem(
                        song = song,
                        onClick = { onSongClick(song, songs) },
                        isPlaying = currentPlayingSong?.id == song.id,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                }
                
                
                item {
                    Spacer(modifier = Modifier.height(100.dp))
                }
            }
        }
    }
}
