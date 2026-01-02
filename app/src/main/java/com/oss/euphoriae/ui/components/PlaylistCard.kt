package com.oss.euphoriae.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlaylistPlay
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.oss.euphoriae.data.model.Playlist

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaylistCard(
    playlist: Playlist,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.width(140.dp),
        onClick = onClick,
        shape = RoundedCornerShape(12.dp)
    ) {
        Column {
            if (playlist.covers.size >= 3) {
                 // 3-Image Collage: 1 Big Left, 2 Small Right Stacked
                 Row(modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
                 ) {
                     // Main Image (Left Half)
                     AsyncImage(
                         model = playlist.covers[0],
                         contentDescription = null,
                         contentScale = ContentScale.Crop,
                         modifier = Modifier.weight(1f).fillMaxHeight()
                     )
                     
                     // Stacked Images (Right Half)
                     Column(modifier = Modifier.weight(1f)) {
                         AsyncImage(
                             model = playlist.covers[1],
                             contentDescription = null,
                             contentScale = ContentScale.Crop,
                             modifier = Modifier.weight(1f).fillMaxWidth()
                         )
                         AsyncImage(
                             model = playlist.covers[2],
                             contentDescription = null,
                             contentScale = ContentScale.Crop,
                             modifier = Modifier.weight(1f).fillMaxWidth()
                         )
                     }
                 }
            } else if (playlist.coverUri != null) {
                AsyncImage(
                    model = playlist.coverUri,
                    contentDescription = playlist.name,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PlaylistPlay,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = playlist.name,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${playlist.songCount} Songs",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
