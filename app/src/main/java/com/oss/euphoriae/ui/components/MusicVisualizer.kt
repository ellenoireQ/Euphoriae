package com.oss.euphoriae.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun MusicVisualizer(
    isPlaying: Boolean,
    modifier: Modifier = Modifier,
    barCount: Int = 5,
    barColor: Color = MaterialTheme.colorScheme.primary,
    containerColor: Color = MaterialTheme.colorScheme.primaryContainer
) {
    val infiniteTransition = rememberInfiniteTransition(label = "visualizer")
    
    // Different animation specs for each bar to create organic movement
    val barAnimations = List(barCount) { index ->
        infiniteTransition.animateFloat(
            initialValue = if (isPlaying) 0.25f else 0.15f,
            targetValue = if (isPlaying) 1f else 0.15f,
            animationSpec = infiniteRepeatable(
                animation = tween(
                    durationMillis = 400 + (index * 80),
                    easing = FastOutSlowInEasing
                ),
                repeatMode = RepeatMode.Reverse
            ),
            label = "bar_$index"
        )
    }

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        color = containerColor,
        tonalElevation = 2.dp
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(24.dp)
            ) {
                barAnimations.forEachIndexed { index, animatedValue ->
                    val height = animatedValue.value
                    
                    Box(
                        modifier = Modifier
                            .width(12.dp)
                            .height((80 * height).dp.coerceAtLeast(12.dp))
                            .clip(RoundedCornerShape(6.dp))
                            .background(barColor.copy(alpha = 0.7f + height * 0.3f))
                    )
                }
            }
        }
    }
}
