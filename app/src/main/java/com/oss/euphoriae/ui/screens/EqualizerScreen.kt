package com.oss.euphoriae.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.oss.euphoriae.data.`class`.AudioEffectsManager
import com.oss.euphoriae.ui.theme.EuphoriaeTheme

data class EqualizerBand(
    val name: String,
    val frequency: String,
    val level: Float = 0f 
)

// Preset configurations: values for [60Hz, 230Hz, 910Hz, 3.6kHz, 14kHz]
private val presetConfigs = mapOf(
    "Custom" to listOf(0f, 0f, 0f, 0f, 0f),
    "Flat" to listOf(0f, 0f, 0f, 0f, 0f),
    "Bass Boost" to listOf(0.8f, 0.5f, 0f, 0f, 0f),
    "Treble Boost" to listOf(0f, 0f, 0f, 0.5f, 0.8f),
    "Rock" to listOf(0.6f, 0.3f, -0.2f, 0.4f, 0.6f),
    "Pop" to listOf(-0.2f, 0.3f, 0.5f, 0.3f, -0.2f),
    "Jazz" to listOf(0.4f, 0.2f, -0.3f, 0.2f, 0.4f),
    "Classical" to listOf(0.3f, 0.1f, 0f, 0.2f, 0.5f)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EqualizerScreen(
    audioEffectsManager: AudioEffectsManager? = null,
    modifier: Modifier = Modifier
) {
    var isEnabled by remember { mutableStateOf(true) }
    var selectedPreset by remember { mutableStateOf("Flat") }
    
    val presets = listOf("Custom", "Flat", "Bass Boost", "Treble Boost", "Rock", "Pop", "Jazz", "Classical")
    
    var bands by remember {
        mutableStateOf(
            listOf(
                EqualizerBand("60", "60Hz", 0f),
                EqualizerBand("230", "230Hz", 0f),
                EqualizerBand("910", "910Hz", 0f),
                EqualizerBand("3.6k", "3.6kHz", 0f),
                EqualizerBand("14k", "14kHz", 0f)
            )
        )
    }
    
    fun applyPreset(preset: String) {
        selectedPreset = preset
        val config = presetConfigs[preset] ?: return
        bands = bands.mapIndexed { index, band ->
            band.copy(level = config.getOrElse(index) { 0f })
        }
        audioEffectsManager?.setAllBandLevels(config)
    }
    
    var bassBoost by remember { mutableFloatStateOf(audioEffectsManager?.getBassBoostLevel() ?: 0f) }
    var virtualizer by remember { mutableFloatStateOf(audioEffectsManager?.getVirtualizerLevel() ?: 0f) }
    
    LaunchedEffect(isEnabled) {
        audioEffectsManager?.setEnabled(isEnabled)
    }
    
    val scrollState = rememberScrollState()
    
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Equalizer",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                actions = {
                    IconButton(
                        onClick = {
                            applyPreset("Flat")
                            bassBoost = 0f
                            virtualizer = 0f
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Reset"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                tonalElevation = 1.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Equalizer",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = if (isEnabled) "Enabled" else "Disabled",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = isEnabled,
                        onCheckedChange = { isEnabled = it }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = "Presets",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            SingleChoiceSegmentedButtonRow(
                modifier = Modifier.fillMaxWidth()
            ) {
                presets.take(4).forEachIndexed { index, preset ->
                    SegmentedButton(
                        selected = selectedPreset == preset,
                        onClick = { applyPreset(preset) },
                        shape = SegmentedButtonDefaults.itemShape(
                            index = index,
                            count = 4
                        ),
                        enabled = isEnabled
                    ) {
                        Text(
                            text = preset,
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            SingleChoiceSegmentedButtonRow(
                modifier = Modifier.fillMaxWidth()
            ) {
                presets.drop(4).forEachIndexed { index, preset ->
                    SegmentedButton(
                        selected = selectedPreset == preset,
                        onClick = { applyPreset(preset) },
                        shape = SegmentedButtonDefaults.itemShape(
                            index = index,
                            count = 4
                        ),
                        enabled = isEnabled
                    ) {
                        Text(
                            text = preset,
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Text(
                text = "Frequency Bands",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                tonalElevation = 1.dp
            ) {
                Column(
                    modifier = Modifier.padding(vertical = 16.dp, horizontal = 8.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "+12dB",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "0dB",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "-12dB",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        bands.forEachIndexed { index, band ->
                            EqualizerBandSlider(
                                band = band,
                                enabled = isEnabled,
                                onValueChange = { newLevel ->
                                    val coercedLevel = newLevel.coerceIn(-1f, 1f)
                                    bands = bands.toMutableList().apply {
                                        this[index] = band.copy(level = coercedLevel)
                                    }
                                    selectedPreset = "Custom"
                                    // Apply to real equalizer
                                    audioEffectsManager?.setBandLevel(index, coercedLevel)
                                }
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Text(
                text = "Audio Effects",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                tonalElevation = 1.dp
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Bass Boost",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "${(bassBoost * 100).toInt()}%",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Slider(
                        value = bassBoost,
                        onValueChange = { 
                            bassBoost = it
                            audioEffectsManager?.setBassBoostLevel(it)
                        },
                        enabled = isEnabled,
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Virtualizer",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "${(virtualizer * 100).toInt()}%",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Slider(
                        value = virtualizer,
                        onValueChange = { 
                            virtualizer = it
                            audioEffectsManager?.setVirtualizerLevel(it)
                        },
                        enabled = isEnabled,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun EqualizerBandSlider(
    band: EqualizerBand,
    enabled: Boolean,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val animatedLevel by animateFloatAsState(
        targetValue = band.level,
        label = "band_level"
    )
    
    val sliderHeight = 140.dp
    val thumbSize = 20.dp
    
    Column(
        modifier = modifier.width(52.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "${if (band.level >= 0) "+" else ""}${(band.level * 12).toInt()}",
            style = MaterialTheme.typography.labelMedium,
            color = if (enabled) {
                when {
                    band.level > 0.1f -> MaterialTheme.colorScheme.primary
                    band.level < -0.1f -> MaterialTheme.colorScheme.error
                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                }
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            },
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Box(
            modifier = Modifier
                .width(36.dp)
                .height(sliderHeight)
                .clip(RoundedCornerShape(18.dp))
                .background(
                    if (enabled) MaterialTheme.colorScheme.surfaceContainerHighest
                    else MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.5f)
                )
                .then(
                    if (enabled) {
                        Modifier.pointerInput(Unit) {
                            detectVerticalDragGestures { change, _ ->
                                change.consume()
                                val y = change.position.y
                                val height = size.height.toFloat()
                                // Convert y position to level (-1 to 1)
                                // Top = +1, Bottom = -1
                                val newLevel = 1f - (y / height * 2f)
                                onValueChange(newLevel.coerceIn(-1f, 1f))
                            }
                        }
                    } else Modifier
                ),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
            )
            
            
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .fillMaxHeight()
                    .padding(vertical = 10.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            )
            
            // Filled portion from center
            val fillFraction = kotlin.math.abs(animatedLevel) / 2f
            val isPositive = animatedLevel >= 0
            
            Box(
                modifier = Modifier
                    .width(6.dp)
                    .fillMaxHeight(fillFraction)
                    .align(if (isPositive) Alignment.TopCenter else Alignment.BottomCenter)
                    .padding(
                        top = if (isPositive) (sliderHeight / 2) - (sliderHeight * fillFraction) else 0.dp,
                        bottom = if (!isPositive) (sliderHeight / 2) - (sliderHeight * fillFraction) else 0.dp
                    )
                    .offset(y = if (isPositive) (sliderHeight * (0.5f - fillFraction)) else (sliderHeight * (fillFraction - 0.5f)))
                    .clip(RoundedCornerShape(3.dp))
                    .background(
                        if (enabled) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                    )
            )
            
            // Thumb
            val thumbOffset = -(animatedLevel * (sliderHeight.value - thumbSize.value) / 2f)
            Box(
                modifier = Modifier
                    .offset(y = thumbOffset.dp)
                    .size(thumbSize)
                    .clip(RoundedCornerShape(thumbSize / 2))
                    .background(
                        if (enabled) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                    )
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Frequency label
        Text(
            text = band.name,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Medium
        )
    }
}

@Preview(showBackground = true)
@Composable
fun EqualizerScreenPreview() {
    EuphoriaeTheme {
        EqualizerScreen()
    }
}
