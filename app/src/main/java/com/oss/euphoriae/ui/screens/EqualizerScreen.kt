@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3ExpressiveApi::class)

package com.oss.euphoriae.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.oss.euphoriae.data.`class`.AudioEffectsManager
import com.oss.euphoriae.data.preferences.EffectProfile
import com.oss.euphoriae.data.preferences.HeadphoneType
import com.oss.euphoriae.data.preferences.ReverbPreset
import com.oss.euphoriae.data.preferences.SurroundMode
import com.oss.euphoriae.engine.AudioEngine
import com.oss.euphoriae.ui.theme.EuphoriaeTheme

data class EqualizerBand(
    val name: String,
    val frequency: String,
    val level: Float = 0f 
)

// Preset configurations for 10-band EQ: [31Hz, 62Hz, 125Hz, 250Hz, 500Hz, 1kHz, 2kHz, 4kHz, 8kHz, 16kHz]
private val presetConfigs = mapOf(
    "Custom" to listOf(0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f),
    "Flat" to listOf(0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f),
    "Bass Boost" to listOf(0.8f, 0.7f, 0.5f, 0.3f, 0f, 0f, 0f, 0f, 0f, 0f),
    "Treble Boost" to listOf(0f, 0f, 0f, 0f, 0f, 0f, 0.3f, 0.5f, 0.7f, 0.8f),
    "Rock" to listOf(0.6f, 0.4f, 0.2f, -0.2f, -0.3f, 0f, 0.3f, 0.5f, 0.6f, 0.6f),
    "Pop" to listOf(-0.2f, 0f, 0.2f, 0.4f, 0.5f, 0.4f, 0.2f, 0f, -0.1f, -0.2f),
    "Jazz" to listOf(0.4f, 0.3f, 0.1f, -0.2f, -0.3f, -0.2f, 0.1f, 0.3f, 0.4f, 0.5f),
    "Classical" to listOf(0.3f, 0.2f, 0.1f, 0f, -0.1f, -0.1f, 0f, 0.2f, 0.4f, 0.5f),
    "Hip Hop" to listOf(0.7f, 0.6f, 0.4f, 0.1f, 0f, -0.1f, 0.1f, 0.2f, 0.3f, 0.3f),
    "Electronic" to listOf(0.6f, 0.5f, 0.2f, 0f, -0.2f, 0f, 0.2f, 0.5f, 0.7f, 0.8f),
    "Vocal" to listOf(-0.3f, -0.2f, 0f, 0.3f, 0.5f, 0.5f, 0.3f, 0f, -0.2f, -0.3f),
    "R&B" to listOf(0.5f, 0.4f, 0.2f, 0.1f, 0f, 0.2f, 0.3f, 0.2f, 0.1f, 0f)
)

@Composable
fun EqualizerScreen(
    audioEffectsManager: AudioEffectsManager? = null,
    audioEngine: AudioEngine? = null,
    audioPreferences: com.oss.euphoriae.data.preferences.AudioPreferences? = null,
    onPlaybackParamsChange: (Float, Float) -> Unit = { _, _ -> },
    modifier: Modifier = Modifier
) {
    // Load initial values from preferences
    var isEnabled by remember { mutableStateOf(audioPreferences?.isEqEnabled() ?: true) }
    var selectedPreset by remember { mutableStateOf(audioPreferences?.getSelectedPreset() ?: "Flat") }
    var selectedProfile by remember { mutableStateOf(audioPreferences?.getEffectProfile() ?: EffectProfile.CUSTOM) }
    
    // 10-band EQ - load from preferences
    val bandNames = listOf("31", "62", "125", "250", "500", "1k", "2k", "4k", "8k", "16k")
    val bandFrequencies = listOf("31Hz", "62Hz", "125Hz", "250Hz", "500Hz", "1kHz", "2kHz", "4kHz", "8kHz", "16kHz")
    var bands by remember {
        mutableStateOf(
            bandNames.mapIndexed { index, name ->
                EqualizerBand(name, bandFrequencies[index], audioPreferences?.getBandLevel(index) ?: 0f)
            }
        )
    }
    
    // Basic Effects - load from preferences
    var bassBoost by remember { mutableFloatStateOf(audioPreferences?.getBassBoost() ?: 0f) }
    var virtualizer by remember { mutableFloatStateOf(audioPreferences?.getVirtualizer() ?: 0f) }
    
    // DSP Settings - load from preferences
    var reverbPreset by remember { mutableStateOf(audioPreferences?.getReverbPreset() ?: ReverbPreset.NONE) }
    var loudnessGain by remember { mutableFloatStateOf(audioPreferences?.getLoudnessGain() ?: 0f) }
    var stereoBalance by remember { mutableFloatStateOf(audioPreferences?.getStereoBalance() ?: 0f) }
    var channelSeparation by remember { mutableFloatStateOf(audioPreferences?.getChannelSeparation() ?: 0.5f) }
    
    // Surround Settings - load from preferences
    var surroundMode by remember { mutableStateOf(audioPreferences?.getSurroundMode() ?: SurroundMode.OFF) }
    var surroundLevel by remember { mutableFloatStateOf(audioPreferences?.getSurroundLevel() ?: 0.5f) }
    var roomSize by remember { mutableFloatStateOf(audioPreferences?.getRoomSize() ?: 0.5f) }
    var effect3d by remember { mutableFloatStateOf(audioPreferences?.get3DEffect() ?: 0f) }
    
    // Headphone Settings - load from preferences
    var headphoneType by remember { mutableStateOf(audioPreferences?.getHeadphoneType() ?: HeadphoneType.GENERIC) }
    var headphoneSurround by remember { mutableStateOf(audioPreferences?.getHeadphoneSurround() ?: false) }
    
    // Dynamic Processing - load from preferences
    var compressor by remember { mutableFloatStateOf(audioPreferences?.getCompressor() ?: 0f) }
    var volumeLeveler by remember { mutableFloatStateOf(audioPreferences?.getVolumeLeveler() ?: 0f) }
    var limiter by remember { mutableFloatStateOf(audioPreferences?.getLimiter() ?: 0f) }
    var dynamicRange by remember { mutableFloatStateOf(audioPreferences?.getDynamicRange() ?: 1f) }
    
    // Tempo/Pitch Control
    var tempo by remember { mutableFloatStateOf(1f) }  // 0.5 to 2.0
    var pitch by remember { mutableFloatStateOf(0f) }  // -12 to +12 semitones
    var crossfade by remember { mutableFloatStateOf(0f) }  // 0 to 12 seconds
    
    // Enhancement - load from preferences
    var clarity by remember { mutableFloatStateOf(audioPreferences?.getClarity() ?: 0f) }
    var spectrumExtension by remember { mutableFloatStateOf(audioPreferences?.getSpectrumExtension() ?: 0f) }
    var tubeAmp by remember { mutableFloatStateOf(audioPreferences?.getTubeAmp() ?: 0f) }
    var trebleBoost by remember { mutableFloatStateOf(audioPreferences?.getTrebleBoost() ?: 0f) }
    
    // Section expansion states
    var expandedSections by remember { 
        mutableStateOf(setOf("equalizer", "effects"))
    }
    
    // Apply saved settings to audio engine on first composition
    LaunchedEffect(Unit) {
        // Apply EQ bands to engine
        bands.forEachIndexed { index, band ->
            audioEngine?.setEqualizerBand(index, band.level * 12f)
        }
        // Apply other effects
        audioEngine?.setBassBoost(bassBoost)
        audioEngine?.setVirtualizer(virtualizer)
        audioEngine?.setStereoBalance(stereoBalance)
        audioEngine?.setChannelSeparation(channelSeparation)
        audioEngine?.setSurroundLevel(surroundLevel)
        audioEngine?.setRoomSize(roomSize)
        audioEngine?.setSurround3D(effect3d)
        audioEngine?.setHeadphoneType(headphoneType.ordinal)
        audioEngine?.setHeadphoneSurround(headphoneSurround)
        audioEngine?.setCompressor(compressor)
        audioEngine?.setVolumeLeveler(volumeLeveler)
        audioEngine?.setLimiter(0.99f - (limiter * 0.49f))
        audioEngine?.setClarity(clarity)
        audioEngine?.setSpectrumExtension(spectrumExtension)
        audioEngine?.setTubeWarmth(tubeAmp)
        audioEngine?.setTrebleBoost(trebleBoost)
        if (reverbPreset != ReverbPreset.NONE) {
            audioEngine?.setReverb(reverbPreset.ordinal, 0.5f)
        }
    }
    
    fun toggleSection(section: String) {
        expandedSections = if (section in expandedSections) {
            expandedSections - section
        } else {
            expandedSections + section
        }
    }
    
    fun applyPreset(preset: String) {
        selectedPreset = preset
        audioPreferences?.setSelectedPreset(preset)
        val config = presetConfigs[preset] ?: return
        bands = bands.mapIndexed { index, band ->
            val newLevel = config.getOrElse(index) { 0f }
            // Save to preferences and apply to engine
            audioPreferences?.setBandLevel(index, newLevel)
            audioEngine?.setEqualizerBand(index, newLevel * 12f)
            band.copy(level = newLevel)
        }
    }
    
    fun applyProfile(profile: EffectProfile) {
        selectedProfile = profile
        audioPreferences?.setEffectProfile(profile)
        when (profile) {
            EffectProfile.CUSTOM -> { /* User controls everything manually */ }
            EffectProfile.MUSIC -> {
                applyPreset("Pop")
                bassBoost = 0.3f; audioPreferences?.setBassBoost(0.3f); audioEngine?.setBassBoost(0.3f)
                virtualizer = 0.4f; audioPreferences?.setVirtualizer(0.4f); audioEngine?.setVirtualizer(0.4f)
                clarity = 0.3f; audioPreferences?.setClarity(0.3f); audioEngine?.setClarity(0.3f)
            }
            EffectProfile.MOVIE -> {
                applyPreset("Flat")
                effect3d = 0.6f; audioPreferences?.set3DEffect(0.6f); audioEngine?.setSurround3D(0.6f)
                roomSize = 0.7f; audioPreferences?.setRoomSize(0.7f); audioEngine?.setRoomSize(0.7f)
                bassBoost = 0.5f; audioPreferences?.setBassBoost(0.5f); audioEngine?.setBassBoost(0.5f)
            }
            EffectProfile.GAME -> {
                applyPreset("Flat")
                effect3d = 0.8f; audioPreferences?.set3DEffect(0.8f); audioEngine?.setSurround3D(0.8f)
                clarity = 0.5f; audioPreferences?.setClarity(0.5f); audioEngine?.setClarity(0.5f)
                bassBoost = 0.6f; audioPreferences?.setBassBoost(0.6f); audioEngine?.setBassBoost(0.6f)
            }
            EffectProfile.PODCAST -> {
                applyPreset("Vocal")
                clarity = 0.7f; audioPreferences?.setClarity(0.7f); audioEngine?.setClarity(0.7f)
                volumeLeveler = 0.6f; audioPreferences?.setVolumeLeveler(0.6f); audioEngine?.setVolumeLeveler(0.6f)
                compressor = 0.4f; audioPreferences?.setCompressor(0.4f); audioEngine?.setCompressor(0.4f)
            }
            EffectProfile.HIFI -> {
                applyPreset("Flat")
                tubeAmp = 0.2f; audioPreferences?.setTubeAmp(0.2f); audioEngine?.setTubeWarmth(0.2f)
                spectrumExtension = 0.3f; audioPreferences?.setSpectrumExtension(0.3f); audioEngine?.setSpectrumExtension(0.3f)
            }
        }
    }
    
    fun resetAll() {
        applyPreset("Flat")
        bassBoost = 0f; audioPreferences?.setBassBoost(0f); audioEngine?.setBassBoost(0f)
        virtualizer = 0f; audioPreferences?.setVirtualizer(0f); audioEngine?.setVirtualizer(0f)
        reverbPreset = ReverbPreset.NONE; audioPreferences?.setReverbPreset(ReverbPreset.NONE)
        loudnessGain = 0f; audioPreferences?.setLoudnessGain(0f)
        stereoBalance = 0f; audioPreferences?.setStereoBalance(0f); audioEngine?.setStereoBalance(0f)
        channelSeparation = 0.5f; audioPreferences?.setChannelSeparation(0.5f); audioEngine?.setChannelSeparation(0.5f)
        surroundMode = SurroundMode.OFF; audioPreferences?.setSurroundMode(SurroundMode.OFF)
        surroundLevel = 0.5f; audioPreferences?.setSurroundLevel(0.5f); audioEngine?.setSurroundLevel(0.5f)
        roomSize = 0.5f; audioPreferences?.setRoomSize(0.5f); audioEngine?.setRoomSize(0.5f)
        effect3d = 0f; audioPreferences?.set3DEffect(0f); audioEngine?.setSurround3D(0f)
        headphoneType = HeadphoneType.GENERIC; audioPreferences?.setHeadphoneType(HeadphoneType.GENERIC); audioEngine?.setHeadphoneType(HeadphoneType.GENERIC.ordinal)
        headphoneSurround = false; audioPreferences?.setHeadphoneSurround(false); audioEngine?.setHeadphoneSurround(false)
        compressor = 0f; audioPreferences?.setCompressor(0f); audioEngine?.setCompressor(0f)
        volumeLeveler = 0f; audioPreferences?.setVolumeLeveler(0f); audioEngine?.setVolumeLeveler(0f)
        limiter = 0f; audioPreferences?.setLimiter(0f); audioEngine?.setLimiter(0.99f)
        dynamicRange = 1f; audioPreferences?.setDynamicRange(1f); audioEngine?.setDynamicRange(1f)
        loudnessGain = 0f; audioPreferences?.setLoudnessGain(0f); audioEngine?.setLoudnessGain(0f)
        clarity = 0f; audioPreferences?.setClarity(0f); audioEngine?.setClarity(0f)
        spectrumExtension = 0f; audioPreferences?.setSpectrumExtension(0f); audioEngine?.setSpectrumExtension(0f)
        tubeAmp = 0f; audioPreferences?.setTubeAmp(0f); audioEngine?.setTubeWarmth(0f)
        trebleBoost = 0f; audioPreferences?.setTrebleBoost(0f); audioEngine?.setTrebleBoost(0f)
        selectedProfile = EffectProfile.CUSTOM; audioPreferences?.setEffectProfile(EffectProfile.CUSTOM)
        audioEffectsManager?.resetAll()
    }
    
    LaunchedEffect(isEnabled) {
        audioPreferences?.setEqEnabled(isEnabled)
        audioEffectsManager?.setEnabled(isEnabled)
    }

    
    val scrollState = rememberScrollState()
    
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Equalizer",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = selectedProfile.displayName,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { resetAll() }) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Reset All"
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
                .padding(horizontal = 16.dp)
                .padding(bottom = 100.dp)
        ) {
            // Master Enable Card
            GradientCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Equalizer,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Audio Engine",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = if (isEnabled) "All effects active" else "Bypass mode",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Switch(
                        checked = isEnabled,
                        onCheckedChange = { isEnabled = it },
                        thumbContent = {
                            Crossfade(
                                targetState = isEnabled,
                                animationSpec = tween(durationMillis = 500),
                            ) { isChecked ->
                                if (isChecked) {
                                    Icon(
                                        imageVector = Icons.Rounded.Check,
                                        contentDescription = null,
                                        modifier = Modifier.size(SwitchDefaults.IconSize),
                                    )
                                }
                            }
                        },
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Effect Profiles
            Text(
                text = "Effect Profiles",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                EffectProfile.entries.take(3).forEach { profile ->
                    ProfileChip(
                        profile = profile,
                        isSelected = selectedProfile == profile,
                        onClick = { applyProfile(profile) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                EffectProfile.entries.drop(3).forEach { profile ->
                    ProfileChip(
                        profile = profile,
                        isSelected = selectedProfile == profile,
                        onClick = { applyProfile(profile) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // ================== EQUALIZER SECTION ==================
            ExpandableSection(
                title = "Graphic Equalizer",
                subtitle = "10-band frequency control",
                icon = Icons.Default.GraphicEq,
                isExpanded = "equalizer" in expandedSections,
                onToggle = { toggleSection("equalizer") }
            ) {
                // Preset Row
                Text(
                    text = "Presets",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                SingleChoiceSegmentedButtonRow(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    listOf("Flat", "Bass Boost", "Rock", "Pop").forEachIndexed { index, preset ->
                        SegmentedButton(
                            selected = selectedPreset == preset,
                            onClick = { applyPreset(preset) },
                            shape = SegmentedButtonDefaults.itemShape(index = index, count = 4),
                            enabled = isEnabled
                        ) {
                            Text(preset, style = MaterialTheme.typography.labelSmall, maxLines = 1)
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                SingleChoiceSegmentedButtonRow(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    listOf("Jazz", "Classical", "Electronic", "Vocal").forEachIndexed { index, preset ->
                        SegmentedButton(
                            selected = selectedPreset == preset,
                            onClick = { applyPreset(preset) },
                            shape = SegmentedButtonDefaults.itemShape(index = index, count = 4),
                            enabled = isEnabled
                        ) {
                            Text(preset, style = MaterialTheme.typography.labelSmall, maxLines = 1)
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(20.dp))
                
                // 10-Band EQ Sliders
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerHighest,
                    tonalElevation = 1.dp
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("+12dB", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("0dB", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("-12dB", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            bands.forEachIndexed { index, band ->
                                CompactBandSlider(
                                    band = band,
                                    enabled = isEnabled,
                                    onValueChange = { newLevel ->
                                        val coercedLevel = newLevel.coerceIn(-1f, 1f)
                                        bands = bands.toMutableList().apply {
                                            this[index] = band.copy(level = coercedLevel)
                                        }
                                        selectedPreset = "Custom"
                                        audioPreferences?.setSelectedPreset("Custom")
                                        // Save to preferences and apply to engine
                                        audioPreferences?.setBandLevel(index, coercedLevel)
                                        audioEngine?.setEqualizerBand(index, coercedLevel * 12f)
                                    }
                                )
                            }
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // ================== AUDIO EFFECTS SECTION ==================
            ExpandableSection(
                title = "Audio Effects",
                subtitle = "Bass, virtualizer & spatial",
                icon = Icons.Default.SurroundSound,
                isExpanded = "effects" in expandedSections,
                onToggle = { toggleSection("effects") }
            ) {
                EffectSlider(
                    label = "Bass Boost",
                    value = bassBoost,
                    onValueChange = { 
                        bassBoost = it
                        audioPreferences?.setBassBoost(it)
                        audioEffectsManager?.setBassBoostLevel(it)
                        audioEngine?.setBassBoost(it)
                    },
                    enabled = isEnabled,
                    icon = Icons.Default.Speaker
                )
                
                EffectSlider(
                    label = "Virtualizer",
                    value = virtualizer,
                    onValueChange = { 
                        virtualizer = it
                        audioPreferences?.setVirtualizer(it)
                        audioEffectsManager?.setVirtualizerLevel(it)
                        audioEngine?.setVirtualizer(it)
                    },
                    enabled = isEnabled,
                    icon = Icons.Default.Headphones
                )
                
                EffectSlider(
                    label = "Loudness",
                    value = loudnessGain,
                    onValueChange = { 
                        loudnessGain = it
                        audioPreferences?.setLoudnessGain(it)
                        audioEffectsManager?.setLoudnessGain(it)
                        audioEngine?.setLoudnessGain(it)
                    },
                    enabled = isEnabled,
                    icon = Icons.AutoMirrored.Default.VolumeUp,
                    valueLabel = "+${(loudnessGain * 10).toInt()} dB"
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // ================== SURROUND SOUND SECTION ==================
            ExpandableSection(
                title = "Surround Sound",
                subtitle = "3D audio & room simulation",
                icon = Icons.Default.SurroundSound,
                isExpanded = "surround" in expandedSections,
                onToggle = { toggleSection("surround") }
            ) {
                Text(
                    text = "Surround Mode",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SurroundMode.entries.forEach { mode ->
                        FilterChip(
                            selected = surroundMode == mode,
                            onClick = { 
                                surroundMode = mode
                                audioPreferences?.setSurroundMode(mode)
                                audioEngine?.setSurroundMode(mode.ordinal)
                                
                                // Update UI sliders to reflect new mode values
                                when (mode) {
                                    SurroundMode.OFF -> {
                                        effect3d = 0f
                                    }
                                    SurroundMode.MUSIC -> {
                                        effect3d = 0.4f; roomSize = 0.3f; surroundLevel = 0.5f
                                    }
                                    SurroundMode.MOVIE -> {
                                        effect3d = 0.7f; roomSize = 0.7f; surroundLevel = 0.6f
                                    }
                                    SurroundMode.GAME -> {
                                        effect3d = 0.8f; roomSize = 0.4f; surroundLevel = 0.7f
                                        headphoneSurround = true
                                    }
                                    SurroundMode.PODCAST -> {
                                        effect3d = 0.2f; roomSize = 0.2f; surroundLevel = 0.3f
                                    }
                                }
                            },
                            label = { Text(mode.displayName, style = MaterialTheme.typography.labelSmall) },
                            modifier = Modifier.weight(1f),
                            enabled = isEnabled
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                EffectSlider(
                    label = "3D Effect",
                    value = effect3d,
                    onValueChange = { 
                        effect3d = it
                        audioPreferences?.set3DEffect(it)
                        audioEngine?.setSurround3D(it)
                    },
                    enabled = isEnabled && surroundMode != SurroundMode.OFF,
                    icon = Icons.Default.ViewInAr
                )
                
                EffectSlider(
                    label = "Room Size",
                    value = roomSize,
                    onValueChange = { 
                        roomSize = it
                        audioPreferences?.setRoomSize(it)
                        audioEngine?.setRoomSize(it)
                    },
                    enabled = isEnabled && surroundMode != SurroundMode.OFF,
                    icon = Icons.Default.MeetingRoom
                )
                
                EffectSlider(
                    label = "Surround Level",
                    value = surroundLevel,
                    onValueChange = { 
                        surroundLevel = it
                        audioPreferences?.setSurroundLevel(it)
                        audioEngine?.setSurroundLevel(it)
                    },
                    enabled = isEnabled && surroundMode != SurroundMode.OFF,
                    icon = Icons.Default.SpatialAudio
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // ================== HEADPHONE SECTION ==================
            ExpandableSection(
                title = "Headphone Optimization",
                subtitle = "Device-specific tuning",
                icon = Icons.Default.Headphones,
                isExpanded = "headphone" in expandedSections,
                onToggle = { toggleSection("headphone") }
            ) {
                Text(
                    text = "Headphone Type",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    HeadphoneType.entries.take(3).forEach { type ->
                        FilterChip(
                            selected = headphoneType == type,
                            onClick = { 
                                headphoneType = type
                                audioPreferences?.setHeadphoneType(type)
                                audioEngine?.setHeadphoneType(type.ordinal)
                            },
                            label = { Text(type.displayName, style = MaterialTheme.typography.labelSmall, maxLines = 1) },
                            modifier = Modifier.weight(1f),
                            enabled = isEnabled
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    HeadphoneType.entries.drop(3).forEach { type ->
                        FilterChip(
                            selected = headphoneType == type,
                            onClick = { 
                                headphoneType = type
                                audioPreferences?.setHeadphoneType(type)
                                audioEngine?.setHeadphoneType(type.ordinal)
                            },
                            label = { Text(type.displayName, style = MaterialTheme.typography.labelSmall) },
                            modifier = Modifier.weight(1f),
                            enabled = isEnabled
                        )
                    }
                }
                
                // Show description for selected headphone type
                Text(
                    text = headphoneType.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 8.dp)
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.SpatialTracking,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Headphone Surround", style = MaterialTheme.typography.bodyMedium)
                    }
                    Switch(
                        checked = headphoneSurround,
                        onCheckedChange = { 
                            headphoneSurround = it
                            audioPreferences?.setHeadphoneSurround(it)
                            audioEngine?.setHeadphoneSurround(it)
                        },
                        enabled = isEnabled,
                        thumbContent = {
                            Crossfade(
                                targetState = headphoneSurround,
                                animationSpec = tween(durationMillis = 500),
                            ) { isChecked ->
                                if (isChecked) {
                                    Icon(
                                        imageVector = Icons.Rounded.Check,
                                        contentDescription = null,
                                        modifier = Modifier.size(SwitchDefaults.IconSize),
                                    )
                                }
                            }
                        },
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // ================== DYNAMIC PROCESSING SECTION ==================
            ExpandableSection(
                title = "Dynamic Processing",
                subtitle = "Compressor & volume control",
                icon = Icons.Default.Compress,
                isExpanded = "dynamic" in expandedSections,
                onToggle = { toggleSection("dynamic") }
            ) {
                EffectSlider(
                    label = "Compressor",
                    value = compressor,
                    onValueChange = { 
                        compressor = it
                        audioPreferences?.setCompressor(it)
                        audioEngine?.setCompressor(it)
                    },
                    enabled = isEnabled,
                    icon = Icons.Default.Compress,
                    description = "Reduce dynamic range"
                )
                
                EffectSlider(
                    label = "Volume Leveler",
                    value = volumeLeveler,
                    onValueChange = { 
                        volumeLeveler = it
                        audioPreferences?.setVolumeLeveler(it)
                        audioEngine?.setVolumeLeveler(it)
                    },
                    enabled = isEnabled,
                    icon = Icons.Default.Tune,
                    description = "Normalize loudness"
                )
                
                EffectSlider(
                    label = "Limiter",
                    value = limiter,
                    onValueChange = { 
                        limiter = it
                        audioPreferences?.setLimiter(it)
                        // Limiter ceiling: 0.5 to 0.99 (lower value = more limiting)
                        audioEngine?.setLimiter(0.99f - (it * 0.49f))
                    },
                    enabled = isEnabled,
                    icon = Icons.Default.DoNotDisturb,
                    description = "Prevent clipping"
                )
                
                EffectSlider(
                    label = "Dynamic Range",
                    value = dynamicRange,
                    onValueChange = { 
                        dynamicRange = it
                        audioPreferences?.setDynamicRange(it)
                        audioEngine?.setDynamicRange(it)
                    },
                    enabled = isEnabled,
                    icon = Icons.Default.SwapVert,
                    valueLabel = "${(dynamicRange * 100).toInt()}%"
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // ================== ENHANCEMENT SECTION ==================
            ExpandableSection(
                title = "Audio Enhancement",
                subtitle = "Clarity, warmth & harmonics",
                icon = Icons.Default.AutoAwesome,
                isExpanded = "enhancement" in expandedSections,
                onToggle = { toggleSection("enhancement") }
            ) {
                EffectSlider(
                    label = "Clarity",
                    value = clarity,
                    onValueChange = { 
                        clarity = it
                        audioPreferences?.setClarity(it)
                        audioEngine?.setClarity(it)
                    },
                    enabled = isEnabled,
                    icon = Icons.Default.Visibility,
                    description = "High frequency detail"
                )
                
                EffectSlider(
                    label = "Spectrum Extension",
                    value = spectrumExtension,
                    onValueChange = { 
                        spectrumExtension = it
                        audioPreferences?.setSpectrumExtension(it)
                        audioEngine?.setSpectrumExtension(it)
                    },
                    enabled = isEnabled,
                    icon = Icons.Default.Waves,
                    description = "Restore high frequencies"
                )
                
                EffectSlider(
                    label = "Tube Amp Warmth",
                    value = tubeAmp,
                    onValueChange = { 
                        tubeAmp = it
                        audioPreferences?.setTubeAmp(it)
                        audioEngine?.setTubeWarmth(it)
                    },
                    enabled = isEnabled,
                    icon = Icons.Default.Whatshot,
                    description = "Analog warmth simulation"
                )
                
                EffectSlider(
                    label = "Treble Boost",
                    value = trebleBoost,
                    onValueChange = { 
                        trebleBoost = it
                        audioPreferences?.setTrebleBoost(it)
                        audioEngine?.setTrebleBoost(it)
                    },
                    enabled = isEnabled,
                    icon = Icons.Default.KeyboardArrowUp
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // ================== REVERB SECTION ==================
            ExpandableSection(
                title = "Reverb",
                subtitle = "Room ambience",
                icon = Icons.Default.Spa,
                isExpanded = "reverb" in expandedSections,
                onToggle = { toggleSection("reverb") }
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ReverbPreset.entries.take(4).forEach { preset ->
                            FilterChip(
                                selected = reverbPreset == preset,
                                onClick = {
                                    reverbPreset = preset
                                    audioPreferences?.setReverbPreset(preset)
                                    // Native engine preset: 0=None, 1=SmallRoom, 2=MediumRoom, 3=LargeRoom, 4=MediumHall, 5=LargeHall, 6=Plate
                                    audioEngine?.setReverb(preset.ordinal, 0.5f)
                                },
                                label = { Text(preset.displayName, style = MaterialTheme.typography.labelSmall) },
                                modifier = Modifier.weight(1f),
                                enabled = isEnabled
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ReverbPreset.entries.drop(4).forEach { preset ->
                            FilterChip(
                                selected = reverbPreset == preset,
                                onClick = {
                                    reverbPreset = preset
                                    audioPreferences?.setReverbPreset(preset)
                                    audioEngine?.setReverb(preset.ordinal, 0.5f)
                                },
                                label = { Text(preset.displayName, style = MaterialTheme.typography.labelSmall) },
                                modifier = Modifier.weight(1f),
                                enabled = isEnabled
                            )
                        }
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // ================== PLAYBACK CONTROL SECTION ==================
            ExpandableSection(
                title = "Playback Control (Beta)",
                subtitle = "Tempo & Pitch",
                icon = Icons.Default.Speed,
                isExpanded = "playback" in expandedSections,
                onToggle = { toggleSection("playback") }
            ) {
                // Tempo slider (0.5x to 2.0x)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Tempo", style = MaterialTheme.typography.bodyMedium)
                    Text(
                        "${(tempo * 100).toInt()}%",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Slider(
                    value = tempo,
                    onValueChange = {
                        tempo = it
                        onPlaybackParamsChange(tempo, pitch)
                    },
                    valueRange = 0.5f..2f,
                    enabled = isEnabled,
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Pitch slider (-12 to +12 semitones)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Pitch", style = MaterialTheme.typography.bodyMedium)
                    Text(
                        when {
                            pitch > 0.5f -> "+${pitch.toInt()} st"
                            pitch < -0.5f -> "${pitch.toInt()} st"
                            else -> "0 st"
                        },
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Slider(
                    value = pitch,
                    onValueChange = {
                        pitch = it
                        onPlaybackParamsChange(tempo, pitch)
                    },
                    valueRange = -12f..12f,
                    enabled = isEnabled,
                    modifier = Modifier.fillMaxWidth()
                )
                
                // Reset buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { 
                            tempo = 1f
                            onPlaybackParamsChange(1f, pitch)
                        },
                        modifier = Modifier.weight(1f),
                        enabled = isEnabled
                    ) {
                        Text("Reset Tempo")
                    }
                    OutlinedButton(
                        onClick = { 
                            pitch = 0f
                            onPlaybackParamsChange(tempo, 0f)
                        },
                        modifier = Modifier.weight(1f),
                        enabled = isEnabled
                    ) {
                        Text("Reset Pitch")
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                
                // Crossfade slider (0 to 12 seconds)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Crossfade", style = MaterialTheme.typography.bodyMedium)
                    Text(
                        if (crossfade < 0.5f) "Off" else "${crossfade.toInt()}s",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Slider(
                    value = crossfade,
                    onValueChange = {
                        crossfade = it
                        com.oss.euphoriae.service.MusicPlaybackService.crossfadeDurationMs = (it * 1000).toLong()
                    },
                    valueRange = 0f..12f,
                    enabled = isEnabled,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // ================== STEREO & BALANCE SECTION ==================
            ExpandableSection(
                title = "Stereo & Balance",
                subtitle = "Spatial positioning",
                icon = Icons.Default.SwapHoriz,
                isExpanded = "stereo" in expandedSections,
                onToggle = { toggleSection("stereo") }
            ) {
                // Stereo Balance
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Stereo Balance", style = MaterialTheme.typography.bodyMedium)
                    Text(
                        when {
                            stereoBalance < -0.1f -> "L ${(-stereoBalance * 100).toInt()}%"
                            stereoBalance > 0.1f -> "R ${(stereoBalance * 100).toInt()}%"
                            else -> "Center"
                        },
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("L", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("R", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                
                Slider(
                    value = stereoBalance,
                    onValueChange = { 
                        stereoBalance = it
                        audioEffectsManager?.setStereoBalance(it)
                        audioEngine?.setStereoBalance(it)
                    },
                    valueRange = -1f..1f,
                    enabled = isEnabled,
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                EffectSlider(
                    label = "Channel Separation",
                    value = channelSeparation,
                    onValueChange = { 
                        channelSeparation = it
                        audioEffectsManager?.setChannelSeparation(it)
                        audioEngine?.setChannelSeparation(it)
                    },
                    enabled = isEnabled,
                    icon = Icons.AutoMirrored.Default.CallSplit,
                    description = "Stereo width"
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

// ================== COMPOSABLE COMPONENTS ==================

@Composable
private fun GradientCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        tonalElevation = 2.dp
    ) {
        Box(
            modifier = Modifier.background(
                Brush.horizontalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                        MaterialTheme.colorScheme.surfaceContainerHigh
                    )
                )
            )
        ) {
            content()
        }
    }
}

@Composable
private fun ProfileChip(
    profile: EffectProfile,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer 
                else MaterialTheme.colorScheme.surfaceContainerHigh,
        border = if (isSelected) null 
                 else androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = profile.displayName,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer 
                        else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun ExpandableSection(
    title: String,
    subtitle: String,
    icon: ImageVector,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    val rotateArrow by animateFloatAsState(
        targetValue = if (isExpanded) 180f else 0f,
        animationSpec = MaterialTheme.motionScheme.slowSpatialSpec()
    )

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        tonalElevation = 1.dp
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onToggle)
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Icon(
                    imageVector = Icons.Default.ExpandMore,
                    contentDescription = if (isExpanded) "Collapse" else "Expand",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.rotate(rotateArrow)
                )
            }
            
            AnimatedVisibility(
                visible = isExpanded,
                enter = fadeIn(
                    animationSpec = MaterialTheme.motionScheme.slowEffectsSpec()
                ) + expandVertically(
                    animationSpec = MaterialTheme.motionScheme.slowSpatialSpec()
                ),
                exit = fadeOut(
                    animationSpec = MaterialTheme.motionScheme.slowEffectsSpec()
                ) + shrinkVertically(
                    animationSpec = MaterialTheme.motionScheme.slowSpatialSpec()
                )
            ) {
                Column(
                    modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
                    content = content
                )
            }
        }
    }
}

@Composable
private fun EffectSlider(
    label: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    enabled: Boolean,
    icon: ImageVector,
    valueLabel: String = "${(value * 100).toInt()}%",
    description: String? = null,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.padding(vertical = 4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = if (enabled) MaterialTheme.colorScheme.onSurfaceVariant 
                           else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = if (enabled) MaterialTheme.colorScheme.onSurface 
                                else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                    if (description != null) {
                        Text(
                            text = description,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                }
            }
            Text(
                text = valueLabel,
                style = MaterialTheme.typography.labelMedium,
                color = if (enabled) MaterialTheme.colorScheme.primary 
                        else MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
            )
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            enabled = enabled,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun CompactBandSlider(
    band: EqualizerBand,
    enabled: Boolean,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val animatedLevel by animateFloatAsState(
        targetValue = band.level,
        label = "band_level"
    )
    
    val sliderHeight = 100.dp
    val thumbSize = 14.dp
    
    Column(
        modifier = modifier.width(28.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .width(24.dp)
                .height(sliderHeight)
                .clip(RoundedCornerShape(12.dp))
                .background(
                    if (enabled) MaterialTheme.colorScheme.surfaceContainerHigh
                    else MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.5f)
                )
                .then(
                    if (enabled) {
                        Modifier.pointerInput(Unit) {
                            detectVerticalDragGestures { change, _ ->
                                change.consume()
                                val y = change.position.y
                                val height = size.height.toFloat()
                                val newLevel = 1f - (y / height * 2f)
                                onValueChange(newLevel.coerceIn(-1f, 1f))
                            }
                        }
                    } else Modifier
                ),
            contentAlignment = Alignment.Center
        ) {
            // Center line
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
            )
            
            // Thumb
            val thumbOffset = -(animatedLevel * (sliderHeight.value - thumbSize.value) / 2f)
            Box(
                modifier = Modifier
                    .offset(y = thumbOffset.dp)
                    .size(thumbSize)
                    .clip(CircleShape)
                    .background(
                        if (enabled) {
                            when {
                                animatedLevel > 0.1f -> MaterialTheme.colorScheme.primary
                                animatedLevel < -0.1f -> MaterialTheme.colorScheme.tertiary
                                else -> MaterialTheme.colorScheme.outline
                            }
                        } else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                    )
            )
        }
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Text(
            text = band.name,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
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
