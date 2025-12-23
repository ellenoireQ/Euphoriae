package com.oss.euphoriae.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import coil.compose.SubcomposeAsyncImageContent
import coil.compose.AsyncImagePainter
import com.oss.euphoriae.data.model.Song

@Composable
fun SongListItem(
    song: Song,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isPlaying: Boolean = false
) {
    ListItem(
        modifier = modifier.clickable(onClick = onClick),
        headlineContent = {
            Text(
                text = song.title,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = if (isPlaying) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
            )
        },
        supportingContent = {
            Text(
                text = "${song.artist} • ${song.album}",
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        leadingContent = {
            SubcomposeAsyncImage(
                model = song.albumArtUri,
                contentDescription = "Album Art",
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            ) {
                val state = painter.state
                if (state is AsyncImagePainter.State.Success) {
                    SubcomposeAsyncImageContent()
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.MusicNote,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        },
        trailingContent = {
            if (isPlaying) {
                MusicVisualizer(
                    isPlaying = true,
                    barCount = 3,
                    barColor = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            } else {
                Text(
                    text = formatDuration(song.duration),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    )
}

private fun formatDuration(durationMs: Long): String {
    val minutes = (durationMs / 1000) / 60
    val seconds = (durationMs / 1000) % 60
    return String.format("%d:%02d", minutes, seconds)
}
