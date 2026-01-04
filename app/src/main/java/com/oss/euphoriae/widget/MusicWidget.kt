package com.oss.euphoriae.widget

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.currentState
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.oss.euphoriae.MainActivity
import com.oss.euphoriae.R

class MusicWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            val state = WidgetState.fromPreferences(currentState())
            
            GlanceTheme {
                WidgetContent(context = context, state = state)
            }
        }
    }
    
    @Composable
    private fun WidgetContent(context: Context, state: WidgetState) {
        val openNowPlayingIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(MainActivity.EXTRA_OPEN_NOW_PLAYING, true)
        }
        
        Box(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(GlanceTheme.colors.surfaceVariant)
                .cornerRadius(16.dp)
                .clickable(actionStartActivity(openNowPlayingIntent))
                .padding(12.dp)
        ) {
            Row(
                modifier = GlanceModifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Album Art
                Box(
                    modifier = GlanceModifier
                        .size(56.dp)
                        .cornerRadius(8.dp)
                        .background(GlanceTheme.colors.secondaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        provider = ImageProvider(R.drawable.ic_launcher_foreground),
                        contentDescription = "Album Art",
                        modifier = GlanceModifier.size(40.dp)
                    )
                }
                
                Spacer(modifier = GlanceModifier.width(12.dp))
                
                // Song Info
                Column(
                    modifier = GlanceModifier.defaultWeight()
                ) {
                    Text(
                        text = state.songTitle,
                        style = TextStyle(
                            color = GlanceTheme.colors.onSurface,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        ),
                        maxLines = 1
                    )
                    Spacer(modifier = GlanceModifier.height(2.dp))
                    Text(
                        text = state.songArtist,
                        style = TextStyle(
                            color = GlanceTheme.colors.onSurfaceVariant,
                            fontSize = 12.sp
                        ),
                        maxLines = 1
                    )
                }
                
                Spacer(modifier = GlanceModifier.width(8.dp))
                
                // Control Buttons
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Previous Button
                    Box(
                        modifier = GlanceModifier
                            .size(40.dp)
                            .cornerRadius(20.dp)
                            .clickable(MusicWidgetActions.previousAction()),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            provider = ImageProvider(R.drawable.ic_widget_previous),
                            contentDescription = "Previous",
                            modifier = GlanceModifier.size(24.dp)
                        )
                    }
                    
                    Spacer(modifier = GlanceModifier.width(4.dp))
                    
                    // Play/Pause Button
                    Box(
                        modifier = GlanceModifier
                            .size(44.dp)
                            .cornerRadius(22.dp)
                            .background(GlanceTheme.colors.primary)
                            .clickable(MusicWidgetActions.playPauseAction()),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            provider = ImageProvider(
                                if (state.isPlaying) R.drawable.ic_widget_pause 
                                else R.drawable.ic_widget_play
                            ),
                            contentDescription = if (state.isPlaying) "Pause" else "Play",
                            modifier = GlanceModifier.size(24.dp)
                        )
                    }
                    
                    Spacer(modifier = GlanceModifier.width(4.dp))
                    
                    // Next Button
                    Box(
                        modifier = GlanceModifier
                            .size(40.dp)
                            .cornerRadius(20.dp)
                            .clickable(MusicWidgetActions.nextAction()),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            provider = ImageProvider(R.drawable.ic_widget_next),
                            contentDescription = "Next",
                            modifier = GlanceModifier.size(24.dp)
                        )
                    }
                }
            }
        }
    }
}
