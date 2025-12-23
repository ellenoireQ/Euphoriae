package com.oss.euphoriae.data.`class`

import android.media.audiofx.BassBoost
import android.media.audiofx.Equalizer
import android.media.audiofx.Virtualizer
import android.util.Log

class AudioEffectsManager {
    
    private var equalizer: Equalizer? = null
    private var bassBoost: BassBoost? = null
    private var virtualizer: Virtualizer? = null
    
    private var audioSessionId: Int = 0
    private var isEnabled: Boolean = true
    
    private var bandLevels: FloatArray = floatArrayOf(0f, 0f, 0f, 0f, 0f)
    
    private var bassBoostLevel: Float = 0f
    private var virtualizerLevel: Float = 0f
    
    fun initialize(audioSessionId: Int) {
        if (audioSessionId == 0) {
            Log.w(TAG, "Invalid audio session ID: 0")
            return
        }
        
        this.audioSessionId = audioSessionId
        
        try {
            release()
            
            equalizer = Equalizer(0, audioSessionId).apply {
                enabled = isEnabled
            }
            
            bassBoost = BassBoost(0, audioSessionId).apply {
                enabled = isEnabled
                if (getStrengthSupported()) {
                    setStrength((bassBoostLevel * 1000).toInt().toShort())
                }
            }
            
            virtualizer = Virtualizer(0, audioSessionId).apply {
                enabled = isEnabled
                if (getStrengthSupported()) {
                    setStrength((virtualizerLevel * 1000).toInt().toShort())
                }
            }
            
            applyBandLevels()
            
            Log.d(TAG, "Audio effects initialized for session: $audioSessionId")
            Log.d(TAG, "Equalizer bands: ${equalizer?.numberOfBands}")
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize audio effects", e)
        }
    }
    
    fun getNumberOfBands(): Int {
        return equalizer?.numberOfBands?.toInt() ?: 5
    }
    
    fun getBandFrequencyRange(band: Int): IntArray {
        return try {
            equalizer?.getBandFreqRange(band.toShort()) ?: intArrayOf(0, 0)
        } catch (e: Exception) {
            intArrayOf(0, 0)
        }
    }
    
    fun getCenterFrequency(band: Int): Int {
        return try {
            equalizer?.getCenterFreq(band.toShort()) ?: 0
        } catch (e: Exception) {
            0
        }
    }
    
    fun getBandLevelRange(): ShortArray {
        return equalizer?.bandLevelRange ?: shortArrayOf(-1500, 1500)
    }
    
    fun setEnabled(enabled: Boolean) {
        this.isEnabled = enabled
        equalizer?.enabled = enabled
        bassBoost?.enabled = enabled
        virtualizer?.enabled = enabled
    }
    
    fun setBandLevel(band: Int, level: Float) {
        if (band >= 0 && band < bandLevels.size) {
            bandLevels[band] = level.coerceIn(-1f, 1f)
            
            try {
                val range = getBandLevelRange()
                val minLevel = range[0]
                val maxLevel = range[1]
                
                val mBLevel = if (level >= 0) {
                    (level * maxLevel).toInt().toShort()
                } else {
                    (level * -minLevel).toInt().toShort()
                }
                
                equalizer?.setBandLevel(band.toShort(), mBLevel)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to set band level", e)
            }
        }
    }
    
    fun setAllBandLevels(levels: List<Float>) {
        levels.forEachIndexed { index, level ->
            setBandLevel(index, level)
        }
    }
    
    fun getBandLevel(band: Int): Float {
        return if (band >= 0 && band < bandLevels.size) {
            bandLevels[band]
        } else {
            0f
        }
    }
    
    fun getAllBandLevels(): List<Float> {
        return bandLevels.toList()
    }
    
    fun setBassBoostLevel(level: Float) {
        bassBoostLevel = level.coerceIn(0f, 1f)
        try {
            bassBoost?.setStrength((bassBoostLevel * 1000).toInt().toShort())
        } catch (e: Exception) {
            Log.e(TAG, "Failed to set bass boost", e)
        }
    }
    
    fun getBassBoostLevel(): Float = bassBoostLevel
    
    fun setVirtualizerLevel(level: Float) {
        virtualizerLevel = level.coerceIn(0f, 1f)
        try {
            virtualizer?.setStrength((virtualizerLevel * 1000).toInt().toShort())
        } catch (e: Exception) {
            Log.e(TAG, "Failed to set virtualizer", e)
        }
    }
    
    fun getVirtualizerLevel(): Float = virtualizerLevel
    
    fun applyPreset(presetName: String): Boolean {
        val presetLevels = PRESETS[presetName] ?: return false
        setAllBandLevels(presetLevels)
        return true
    }
    
    fun getPresets(): List<String> = PRESETS.keys.toList()
    
    fun reset() {
        setAllBandLevels(listOf(0f, 0f, 0f, 0f, 0f))
        setBassBoostLevel(0f)
        setVirtualizerLevel(0f)
    }
    
    private fun applyBandLevels() {
        bandLevels.forEachIndexed { index, level ->
            setBandLevel(index, level)
        }
    }
    
    fun release() {
        try {
            equalizer?.release()
            bassBoost?.release()
            virtualizer?.release()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to release audio effects", e)
        }
        equalizer = null
        bassBoost = null
        virtualizer = null
    }
    
    companion object {
        private const val TAG = "AudioEffectsManager"
        
        val PRESETS = mapOf(
            "Flat" to listOf(0f, 0f, 0f, 0f, 0f),
            "Bass Boost" to listOf(0.8f, 0.5f, 0f, 0f, 0f),
            "Treble Boost" to listOf(0f, 0f, 0f, 0.5f, 0.8f),
            "Rock" to listOf(0.6f, 0.3f, -0.2f, 0.4f, 0.6f),
            "Pop" to listOf(-0.2f, 0.3f, 0.5f, 0.3f, -0.2f),
            "Jazz" to listOf(0.4f, 0.2f, -0.3f, 0.2f, 0.4f),
            "Classical" to listOf(0.3f, 0.1f, 0f, 0.2f, 0.5f),
            "Hip Hop" to listOf(0.7f, 0.4f, 0f, 0.2f, 0.4f),
            "Electronic" to listOf(0.6f, 0.2f, 0f, 0.3f, 0.7f)
        )
    }
}
