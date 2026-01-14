/*
 * Copyright 2026 Euphoriae
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

#include "audio_engine.h"
#include <android/log.h>
#include <algorithm>
#include <chrono>

#define LOG_TAG "EuphoriaeAudio"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)

namespace euphoriae {

AudioEngine::AudioEngine() {
    LOGI("AudioEngine created with full DSP pipeline");
    // Initialize delay buffers
    std::fill(std::begin(mDelayBufferL), std::end(mDelayBufferL), 0.0f);
    std::fill(std::begin(mDelayBufferR), std::end(mDelayBufferR), 0.0f);
    
    // Initialize reverb comb and allpass buffers (CRITICAL to prevent crackling)
    std::fill(std::begin(mCombBuffer1), std::end(mCombBuffer1), 0.0f);
    std::fill(std::begin(mCombBuffer2), std::end(mCombBuffer2), 0.0f);
    std::fill(std::begin(mCombBuffer3), std::end(mCombBuffer3), 0.0f);
    std::fill(std::begin(mCombBuffer4), std::end(mCombBuffer4), 0.0f);
    std::fill(std::begin(mAllpassBuffer1), std::end(mAllpassBuffer1), 0.0f);
    std::fill(std::begin(mAllpassBuffer2), std::end(mAllpassBuffer2), 0.0f);
}

void AudioEngine::processAudio(float* buffer, int32_t numFrames, int32_t channelCount) {
    if (buffer == nullptr || numFrames <= 0) return;
    
    auto startTime = std::chrono::high_resolution_clock::now();
    
    // ================== DSP Processing Chain ==================
    
    // 1. Input gain / Volume Leveler
    float volumeLeveler = mVolumeLeveler.load();
    if (volumeLeveler > 0.01f) {
        applyVolumeLeveler(buffer, numFrames, channelCount);
    }
    
    // 2. Bass Boost
    float bassBoost = mBassBoost.load();
    if (bassBoost > 0.01f) {
        applyBassBoost(buffer, numFrames, channelCount);
    }
    
    // 3. Treble Boost
    float trebleBoost = mTrebleBoost.load();
    if (trebleBoost > 0.01f) {
        applyTrebleBoost(buffer, numFrames, channelCount);
    }
    
    // 4. Equalizer
    applyEqualizer(buffer, numFrames, channelCount);
    
    // 5. Clarity
    float clarity = mClarity.load();
    if (clarity > 0.01f) {
        applyClarity(buffer, numFrames, channelCount);
    }
    
    // 6. Tube Amp Warmth
    float tubeWarmth = mTubeWarmth.load();
    if (tubeWarmth > 0.01f) {
        applyTubeWarmth(buffer, numFrames * channelCount);
    }
    
    // 7. Spectrum Extension
    float spectrumExt = mSpectrumExtension.load();
    if (spectrumExt > 0.01f) {
        applySpectrumExtension(buffer, numFrames, channelCount);
    }
    
    // 8. Compressor
    float compressor = mCompressorStrength.load();
    if (compressor > 0.01f) {
        applyCompressor(buffer, numFrames, channelCount);
    }
    
    // 8.25 Loudness Gain (makeup gain after compression)
    float loudnessGain = mLoudnessGain.load();
    if (loudnessGain > 0.01f) {
        float gainFactor = 1.0f + (loudnessGain * 1.5f);  // Up to +6dB gain
        int numSamples = numFrames * channelCount;
        for (int32_t i = 0; i < numSamples; i++) {
            buffer[i] *= gainFactor;
        }
    }
    
    // 8.5 Reverb
    int reverbPreset = mReverbPreset.load();
    if (reverbPreset > 0) {
        applyReverb(buffer, numFrames, channelCount);
    }
    
    // 9. Stereo processing
    if (channelCount == 2) {
        // Virtualizer
        float virtualizer = mVirtualizer.load();
        if (virtualizer > 0.01f) {
            applyVirtualizer(buffer, numFrames, channelCount);
        }
        
        // 3D Surround
        float surround3D = mSurround3D.load();
        if (surround3D > 0.01f) {
            applySurround3D(buffer, numFrames);
        }
        
        // Channel Separation
        float separation = mChannelSeparation.load();
        if (std::abs(separation - 0.5f) > 0.01f) {
            applyChannelSeparation(buffer, numFrames);
        }
        
        // Stereo Balance
        float balance = mStereoBalance.load();
        if (std::abs(balance) > 0.01f) {
            applyStereoBalance(buffer, numFrames);
        }
    }
    
    // 10. Limiter
    applyLimiter(buffer, numFrames * channelCount);
    
    // 11. Master Volume
    float volume = mVolume.load();
    if (std::abs(volume - 1.0f) > 0.001f) {
        applyVolume(buffer, numFrames * channelCount);
    }
    
    // 12. Final Hard Clip - prevent any remaining samples > 1.0
    int numSamples = numFrames * channelCount;
    for (int32_t i = 0; i < numSamples; i++) {
        buffer[i] = std::clamp(buffer[i], -1.0f, 1.0f);
    }
    
    // Performance logging
    auto endTime = std::chrono::high_resolution_clock::now();
    auto duration = std::chrono::duration_cast<std::chrono::microseconds>(endTime - startTime);
    
    static int bufferCount = 0;
    bufferCount++;
    if (bufferCount % 500 == 0) {
        float latencyMs = duration.count() / 1000.0f;
        LOGI("DSP latency: %.3f ms | Frames: %d", latencyMs, numFrames);
    }
}

// ================== Setter Implementations ==================

void AudioEngine::setVolume(float volume) {
    mVolume.store(std::clamp(volume, 0.0f, 2.0f));
}

void AudioEngine::setBassBoost(float strength) {
    mBassBoost.store(std::clamp(strength, 0.0f, 1.0f));
}

void AudioEngine::setVirtualizer(float strength) {
    mVirtualizer.store(std::clamp(strength, 0.0f, 1.0f));
}

void AudioEngine::setEqualizerBand(int band, float gainDb) {
    if (band >= 0 && band < kNumEqualizerBands) {
        mEqualizerBands[band].store(std::clamp(gainDb, -12.0f, 12.0f));
    }
}

void AudioEngine::setCompressor(float threshold, float ratio, float attack, float release) {
    mCompressorThreshold.store(threshold);
    mCompressorRatio.store(ratio);
    mCompressorAttack.store(attack);
    mCompressorRelease.store(release);
}

void AudioEngine::setCompressorStrength(float strength) {
    mCompressorStrength.store(std::clamp(strength, 0.0f, 1.0f));
    // Auto-configure compressor based on strength
    mCompressorThreshold.store(-20.0f + (strength * 10.0f));  // -20 to -10 dB
    mCompressorRatio.store(1.0f + (strength * 7.0f));  // 1:1 to 8:1
}

void AudioEngine::setLimiter(float ceiling) {
    mLimiterCeiling.store(std::clamp(ceiling, 0.5f, 1.0f));
}

void AudioEngine::setSurround3D(float depth) {
    mSurround3D.store(std::clamp(depth, 0.0f, 1.0f));
}

void AudioEngine::setRoomSize(float size) {
    mRoomSize.store(std::clamp(size, 0.0f, 1.0f));
}

void AudioEngine::setSurroundLevel(float level) {
    mSurroundLevel.store(std::clamp(level, 0.0f, 1.0f));
}

void AudioEngine::setSurroundMode(int mode) {
    mSurroundMode.store(std::clamp(mode, 0, 4));
    
    // Apply mode-specific presets
    switch (mode) {
        case 0:  // Off - disable surround processing
            mSurround3D.store(0.0f);
            break;
            
        case 1:  // Music - balanced stereo widening with warmth
            mSurround3D.store(0.4f);
            mRoomSize.store(0.3f);
            mSurroundLevel.store(0.5f);
            break;
            
        case 2:  // Movie - immersive with larger room
            mSurround3D.store(0.7f);
            mRoomSize.store(0.7f);
            mSurroundLevel.store(0.6f);
            break;
            
        case 3:  // Game - precise positioning, less reverb
            mSurround3D.store(0.8f);
            mRoomSize.store(0.4f);
            mSurroundLevel.store(0.7f);
            mHeadphoneSurround.store(true);
            break;
            
        case 4:  // Podcast - subtle spatialization, voice focus
            mSurround3D.store(0.2f);
            mRoomSize.store(0.2f);
            mSurroundLevel.store(0.3f);
            break;
    }
}

void AudioEngine::setHeadphoneSurround(bool enabled) {
    mHeadphoneSurround.store(enabled);
}

void AudioEngine::setHeadphoneType(int type) {
    mHeadphoneType.store(std::clamp(type, 0, 4));
}

void AudioEngine::setClarity(float level) {
    mClarity.store(std::clamp(level, 0.0f, 1.0f));
}

void AudioEngine::setTubeWarmth(float warmth) {
    mTubeWarmth.store(std::clamp(warmth, 0.0f, 1.0f));
}

void AudioEngine::setSpectrumExtension(float level) {
    mSpectrumExtension.store(std::clamp(level, 0.0f, 1.0f));
}

void AudioEngine::setStereoBalance(float balance) {
    mStereoBalance.store(std::clamp(balance, -1.0f, 1.0f));
}

void AudioEngine::setChannelSeparation(float separation) {
    mChannelSeparation.store(std::clamp(separation, 0.0f, 1.0f));
}

void AudioEngine::setTrebleBoost(float level) {
    mTrebleBoost.store(std::clamp(level, 0.0f, 1.0f));
}

void AudioEngine::setVolumeLeveler(float level) {
    mVolumeLeveler.store(std::clamp(level, 0.0f, 1.0f));
}

void AudioEngine::setTempo(float tempo) {
    mTempo.store(std::clamp(tempo, 0.5f, 2.0f));
}

void AudioEngine::setPitch(float semitones) {
    mPitchSemitones.store(std::clamp(semitones, -12.0f, 12.0f));
    // Convert semitones to pitch ratio: 2^(semitones/12)
    mPitchRatio = std::pow(2.0f, semitones / 12.0f);
}

void AudioEngine::setDynamicRange(float range) {
    mDynamicRange.store(std::clamp(range, 0.0f, 1.0f));
    // Lower dynamic range = more compression
    // Adjust compressor settings based on dynamic range
    float compressionAmount = 1.0f - range;
    if (compressionAmount > 0.01f) {
        mCompressorStrength.store(compressionAmount * 0.7f);
        mCompressorThreshold.store(-20.0f + (range * 10.0f));  // -20 to -10 dB
        mCompressorRatio.store(1.0f + ((1.0f - range) * 7.0f));  // 1:1 to 8:1
    }
}

void AudioEngine::setLoudnessGain(float gain) {
    mLoudnessGain.store(std::clamp(gain, 0.0f, 1.0f));
}

// ================== DSP Algorithm Implementations ==================

void AudioEngine::applyBassBoost(float* buffer, int32_t numFrames, int32_t channelCount) {
    float strength = mBassBoost.load();
    
    // Low-pass filter for bass extraction
    const float alpha = 0.15f + (strength * 0.15f);
    const float boost = 1.0f + (strength * 2.0f);
    
    for (int32_t i = 0; i < numFrames; i++) {
        for (int32_t ch = 0; ch < std::min(channelCount, 2); ch++) {
            int idx = i * channelCount + ch;
            float sample = buffer[idx];
            
            // Low-pass to extract bass
            mBassState[ch] = mBassState[ch] + alpha * (sample - mBassState[ch]);
            
            // Add boosted bass
            buffer[idx] = sample + (mBassState[ch] * (boost - 1.0f));
        }
    }
}

void AudioEngine::applyTrebleBoost(float* buffer, int32_t numFrames, int32_t channelCount) {
    float strength = mTrebleBoost.load();
    
    // High-pass filter for treble extraction
    const float alpha = 0.9f - (strength * 0.2f);
    const float boost = strength * 1.5f;
    
    for (int32_t i = 0; i < numFrames; i++) {
        for (int32_t ch = 0; ch < std::min(channelCount, 2); ch++) {
            int idx = i * channelCount + ch;
            float sample = buffer[idx];
            
            float prevState = mTrebleState[ch];
            mTrebleState[ch] = sample;
            
            // High-pass to extract treble
            float treble = sample - alpha * prevState - (1.0f - alpha) * mTrebleState[ch];
            
            // Add boosted treble
            buffer[idx] = sample + (treble * boost);
        }
    }
}

void AudioEngine::applyVirtualizer(float* buffer, int32_t numFrames, int32_t channelCount) {
    if (channelCount != 2) return;
    
    float strength = mVirtualizer.load();
    
    // Cross-channel mixing for stereo widening
    const float crossMix = strength * 0.5f;
    const float directGain = 1.0f + (strength * 0.2f);
    
    for (int32_t i = 0; i < numFrames; i++) {
        int idx = i * 2;
        float left = buffer[idx];
        float right = buffer[idx + 1];
        
        // Stereo widening
        buffer[idx] = (left * directGain) - (right * crossMix);
        buffer[idx + 1] = (right * directGain) - (left * crossMix);
    }
}

void AudioEngine::applyEqualizer(float* buffer, int32_t numFrames, int32_t channelCount) {
    // Check if any band has gain
    bool hasGain = false;
    float totalGain = 0.0f;
    
    for (int i = 0; i < kNumEqualizerBands; i++) {
        float bandGain = mEqualizerBands[i].load();
        if (std::abs(bandGain) > 0.1f) {
            hasGain = true;
            totalGain += bandGain;
        }
    }
    if (!hasGain) return;
    
    // Average gain across bands
    totalGain = totalGain / kNumEqualizerBands;
    float linearGain = std::pow(10.0f, totalGain / 20.0f);
    
    for (int32_t i = 0; i < numFrames * channelCount; i++) {
        buffer[i] *= linearGain;
    }
}

void AudioEngine::applyCompressor(float* buffer, int32_t numFrames, int32_t channelCount) {
    float threshold = mCompressorThreshold.load();
    float ratio = mCompressorRatio.load();
    float attack = mCompressorAttack.load();
    float release = mCompressorRelease.load();
    
    // Convert threshold to linear
    float thresholdLin = std::pow(10.0f, threshold / 20.0f);
    
    // Attack/release coefficients
    float attackCoef = std::exp(-1.0f / (attack * 48000.0f));
    float releaseCoef = std::exp(-1.0f / (release * 48000.0f));
    
    for (int32_t i = 0; i < numFrames; i++) {
        // Compute input level
        float inputLevel = 0.0f;
        for (int32_t ch = 0; ch < channelCount; ch++) {
            inputLevel = std::max(inputLevel, std::abs(buffer[i * channelCount + ch]));
        }
        
        // Envelope follower
        if (inputLevel > mCompressorEnvelope) {
            mCompressorEnvelope = attackCoef * mCompressorEnvelope + (1.0f - attackCoef) * inputLevel;
        } else {
            mCompressorEnvelope = releaseCoef * mCompressorEnvelope + (1.0f - releaseCoef) * inputLevel;
        }
        
        // Calculate gain reduction
        float gain = 1.0f;
        if (mCompressorEnvelope > thresholdLin) {
            float overshoot = mCompressorEnvelope / thresholdLin;
            float targetGain = std::pow(overshoot, 1.0f / ratio - 1.0f);
            gain = targetGain;
        }
        
        // Apply gain to all channels
        for (int32_t ch = 0; ch < channelCount; ch++) {
            buffer[i * channelCount + ch] *= gain;
        }
    }
}

void AudioEngine::applyLimiter(float* buffer, int32_t numSamples) {
    float ceiling = mLimiterCeiling.load();
    
    for (int32_t i = 0; i < numSamples; i++) {
        // Soft tanh limiting
        float sample = buffer[i];
        if (std::abs(sample) > ceiling) {
            buffer[i] = ceiling * std::tanh(sample / ceiling);
        }
    }
}

void AudioEngine::applySurround3D(float* buffer, int32_t numFrames) {
    float depth = mSurround3D.load();
    float roomSize = mRoomSize.load();
    float surroundLevel = mSurroundLevel.load();
    bool headphoneSurround = mHeadphoneSurround.load();
    int headphoneType = mHeadphoneType.load();
    
    // Combined effect strength from depth and surround level
    float effectStrength = depth * (0.5f + surroundLevel * 0.5f);
    
    // Headphone-specific adjustments
    float crossfeedAmount = 0.3f;  // Base crossfeed
    float delayMultiplier = 1.0f;
    float bassEnhance = 0.0f;
    float highFreqBoost = 0.0f;
    
    if (headphoneSurround) {
        // Adjust based on headphone type
        switch (headphoneType) {
            case 0:  // Generic
                crossfeedAmount = 0.25f;
                delayMultiplier = 1.0f;
                break;
            case 1:  // In-Ear - more intimate, less delay needed
                crossfeedAmount = 0.20f;
                delayMultiplier = 0.7f;
                bassEnhance = 0.15f;  // In-ears often lack bass
                break;
            case 2:  // Over-Ear - fuller sound, more natural crossfeed
                crossfeedAmount = 0.35f;
                delayMultiplier = 1.2f;
                highFreqBoost = 0.1f;
                break;
            case 3:  // Open-Back - natural soundstage, minimal processing
                crossfeedAmount = 0.15f;
                delayMultiplier = 1.5f;
                break;
            case 4:  // Studio - accurate, moderate crossfeed
                crossfeedAmount = 0.28f;
                delayMultiplier = 1.0f;
                highFreqBoost = 0.05f;
                break;
        }
    }
    
    // Delay time based on room size (0.5ms to 30ms), adjusted by headphone type
    int delayFrames = static_cast<int>((0.5f + roomSize * 29.5f) * 48.0f * delayMultiplier);
    delayFrames = std::min(delayFrames, kMaxDelayFrames - 1);
    
    // Secondary delay for HRTF-like effect (interaural time difference)
    int itdDelay = static_cast<int>(15.0f * delayMultiplier);  // ~0.3ms ITD simulation
    itdDelay = std::min(itdDelay, kMaxDelayFrames - 1);
    
    for (int32_t i = 0; i < numFrames; i++) {
        int idx = i * 2;
        float left = buffer[idx];
        float right = buffer[idx + 1];
        
        // Get delayed samples for room simulation
        int readPos = (mDelayWritePos - delayFrames + kMaxDelayFrames) % kMaxDelayFrames;
        float delayedL = mDelayBufferL[readPos];
        float delayedR = mDelayBufferR[readPos];
        
        // Get ITD delayed samples for spatial cue
        int itdReadPos = (mDelayWritePos - itdDelay + kMaxDelayFrames) % kMaxDelayFrames;
        float itdDelayedL = mDelayBufferL[itdReadPos];
        float itdDelayedR = mDelayBufferR[itdReadPos];
        
        // Write to delay buffer
        mDelayBufferL[mDelayWritePos] = left;
        mDelayBufferR[mDelayWritePos] = right;
        mDelayWritePos = (mDelayWritePos + 1) % kMaxDelayFrames;
        
        // Cross-mix with delayed signal for 3D effect
        float crossGain = effectStrength * crossfeedAmount;
        
        // Apply surround processing
        float newLeft = left + delayedR * crossGain;
        float newRight = right + delayedL * crossGain;
        
        // Add ITD crossfeed for more natural spatialization (if headphone surround enabled)
        if (headphoneSurround) {
            float itdGain = effectStrength * 0.15f;
            newLeft += itdDelayedR * itdGain;
            newRight += itdDelayedL * itdGain;
            
            // Apply headphone-specific enhancements
            if (bassEnhance > 0.0f) {
                // Simple bass emphasis for in-ear headphones
                float mid = (left + right) * 0.5f;
                float bass = mid * bassEnhance * effectStrength;
                newLeft += bass;
                newRight += bass;
            }
            
            if (highFreqBoost > 0.0f) {
                // Simple high frequency emphasis
                float diff = (left - right) * highFreqBoost * effectStrength;
                newLeft += diff;
                newRight -= diff;
            }
        }
        
        buffer[idx] = newLeft;
        buffer[idx + 1] = newRight;
    }
}

void AudioEngine::applyClarity(float* buffer, int32_t numFrames, int32_t channelCount) {
    float level = mClarity.load();
    
    // High-shelf boost around 3-8kHz
    const float alpha = 0.85f;
    const float boost = level * 2.0f;
    
    for (int32_t i = 0; i < numFrames; i++) {
        for (int32_t ch = 0; ch < std::min(channelCount, 2); ch++) {
            int idx = i * channelCount + ch;
            float sample = buffer[idx];
            
            // High-pass to extract high frequencies
            float highFreq = sample - mClarityState[ch] * alpha;
            mClarityState[ch] = sample;
            
            // Add presence
            buffer[idx] = sample + (highFreq * boost);
        }
    }
}

void AudioEngine::applyTubeWarmth(float* buffer, int32_t numSamples) {
    float warmth = mTubeWarmth.load();
    
    // Asymmetric soft clipping for tube simulation
    for (int32_t i = 0; i < numSamples; i++) {
        float sample = buffer[i];
        
        // Asymmetric waveshaping
        float drive = 1.0f + warmth * 3.0f;
        sample = sample * drive;
        
        // Asymmetric saturation
        if (sample > 0) {
            sample = std::tanh(sample * 0.8f) / 0.8f;
        } else {
            sample = std::tanh(sample * 1.2f) / 1.2f;
        }
        
        // Blend dry/wet
        buffer[i] = buffer[i] * (1.0f - warmth) + sample * warmth / drive;
    }
}

void AudioEngine::applySpectrumExtension(float* buffer, int32_t numFrames, int32_t channelCount) {
    float level = mSpectrumExtension.load();
    
    // Generate harmonics to extend high frequencies
    for (int32_t i = 0; i < numFrames; i++) {
        for (int32_t ch = 0; ch < std::min(channelCount, 2); ch++) {
            int idx = i * channelCount + ch;
            float sample = buffer[idx];
            
            // Full-wave rectification generates harmonics
            float harmonic = std::abs(sample) - 0.5f;
            harmonic = std::max(0.0f, harmonic) * 2.0f;
            
            // High-pass the harmonics
            float filteredHarmonic = harmonic - mHarmonicState[ch] * 0.95f;
            mHarmonicState[ch] = harmonic;
            
            // Mix in
            buffer[idx] = sample + filteredHarmonic * level * 0.3f;
        }
    }
}

void AudioEngine::applyStereoBalance(float* buffer, int32_t numFrames) {
    float balance = mStereoBalance.load();
    
    // Equal-power panning
    float leftGain = std::cos(balance * 0.5f * 3.14159f);
    float rightGain = std::sin((balance + 1.0f) * 0.25f * 3.14159f);
    
    if (balance < 0) {
        rightGain = 1.0f + balance;  // Reduce right
        leftGain = 1.0f;
    } else {
        leftGain = 1.0f - balance;  // Reduce left
        rightGain = 1.0f;
    }
    
    for (int32_t i = 0; i < numFrames; i++) {
        buffer[i * 2] *= leftGain;
        buffer[i * 2 + 1] *= rightGain;
    }
}

void AudioEngine::applyChannelSeparation(float* buffer, int32_t numFrames) {
    float separation = mChannelSeparation.load();
    
    // 0 = mono, 0.5 = normal, 1 = extra wide
    float crossMix = (1.0f - separation) * 0.5f;  // More mix = less separation
    float directGain = 0.5f + separation * 0.5f;
    
    for (int32_t i = 0; i < numFrames; i++) {
        int idx = i * 2;
        float left = buffer[idx];
        float right = buffer[idx + 1];
        
        buffer[idx] = left * directGain + right * crossMix;
        buffer[idx + 1] = right * directGain + left * crossMix;
    }
}

void AudioEngine::applyVolumeLeveler(float* buffer, int32_t numFrames, int32_t channelCount) {
    float strength = mVolumeLeveler.load();
    
    // Calculate RMS of this buffer
    float sumSquares = 0.0f;
    int numSamples = numFrames * channelCount;
    
    for (int32_t i = 0; i < numSamples; i++) {
        sumSquares += buffer[i] * buffer[i];
    }
    
    float rms = std::sqrt(sumSquares / numSamples);
    
    // Smooth RMS tracking
    mRmsLevel = mRmsLevel * 0.99f + rms * 0.01f;
    
    // Calculate gain to reach target RMS
    if (mRmsLevel > 0.001f) {
        float targetGain = mTargetRms / mRmsLevel;
        targetGain = std::clamp(targetGain, 0.1f, 4.0f);  // Limit gain range
        
        // Blend based on strength
        float gain = 1.0f + (targetGain - 1.0f) * strength;
        
        for (int32_t i = 0; i < numSamples; i++) {
            buffer[i] *= gain;
        }
    }
}

void AudioEngine::applyVolume(float* buffer, int32_t numSamples) {
    float volume = mVolume.load();
    for (int32_t i = 0; i < numSamples; i++) {
        buffer[i] *= volume;
    }
}

void AudioEngine::setReverb(int preset, float wetMix) {
    mReverbPreset.store(std::clamp(preset, 0, 6));
    mReverbWet.store(std::clamp(wetMix, 0.0f, 1.0f));
}

void AudioEngine::applyReverb(float* buffer, int32_t numFrames, int32_t channelCount) {
    int preset = mReverbPreset.load();
    float wetMix = mReverbWet.load();
    
    if (preset == 0 || wetMix < 0.01f) return;  // None preset or no wet
    
    // Reverb parameters based on preset
    // Decay times (in samples at 48kHz)
    int combDelays[4];
    float combDecays[4];
    int allpassDelays[2];
    
    switch (preset) {
        case 1:  // Small Room
            combDelays[0] = 557; combDelays[1] = 617; combDelays[2] = 709; combDelays[3] = 811;
            combDecays[0] = 0.7f; combDecays[1] = 0.68f; combDecays[2] = 0.66f; combDecays[3] = 0.64f;
            allpassDelays[0] = 113; allpassDelays[1] = 271;
            break;
        case 2:  // Medium Room
            combDelays[0] = 1117; combDelays[1] = 1277; combDelays[2] = 1487; combDelays[3] = 1687;
            combDecays[0] = 0.78f; combDecays[1] = 0.76f; combDecays[2] = 0.74f; combDecays[3] = 0.72f;
            allpassDelays[0] = 211; allpassDelays[1] = 379;
            break;
        case 3:  // Large Room
            combDelays[0] = 1557; combDelays[1] = 1777; combDelays[2] = 2087; combDelays[3] = 2387;
            combDecays[0] = 0.82f; combDecays[1] = 0.80f; combDecays[2] = 0.78f; combDecays[3] = 0.76f;
            allpassDelays[0] = 307; allpassDelays[1] = 491;
            break;
        case 4:  // Medium Hall
            combDelays[0] = 2001; combDelays[1] = 2287; combDelays[2] = 2647; combDelays[3] = 3001;
            combDecays[0] = 0.86f; combDecays[1] = 0.84f; combDecays[2] = 0.82f; combDecays[3] = 0.80f;
            allpassDelays[0] = 403; allpassDelays[1] = 607;
            break;
        case 5:  // Large Hall
            combDelays[0] = 2777; combDelays[1] = 3167; combDelays[2] = 3607; combDelays[3] = 4091;
            combDecays[0] = 0.90f; combDecays[1] = 0.88f; combDecays[2] = 0.86f; combDecays[3] = 0.84f;
            allpassDelays[0] = 509; allpassDelays[1] = 797;
            break;
        case 6:  // Plate
        default:
            combDelays[0] = 1367; combDelays[1] = 1559; combDelays[2] = 1783; combDelays[3] = 2017;
            combDecays[0] = 0.92f; combDecays[1] = 0.91f; combDecays[2] = 0.90f; combDecays[3] = 0.89f;
            allpassDelays[0] = 157; allpassDelays[1] = 331;
            break;
    }
    
    float dryMix = 1.0f - wetMix * 0.5f;  // Keep some dry signal
    const float allpassGain = 0.5f;
    
    for (int32_t i = 0; i < numFrames; i++) {
        // Get mono input for reverb
        float input = 0.0f;
        for (int32_t ch = 0; ch < channelCount; ch++) {
            input += buffer[i * channelCount + ch];
        }
        input /= channelCount;
        
        // 4 Parallel Comb Filters
        float combOut = 0.0f;
        
        // Comb 1
        int readPos1 = (mCombPos1 - combDelays[0] + kReverbBufferSize) % kReverbBufferSize;
        float comb1 = mCombBuffer1[readPos1];
        mCombBuffer1[mCombPos1] = input + comb1 * combDecays[0];
        mCombPos1 = (mCombPos1 + 1) % kReverbBufferSize;
        combOut += comb1;
        
        // Comb 2
        int readPos2 = (mCombPos2 - combDelays[1] + kReverbBufferSize) % kReverbBufferSize;
        float comb2 = mCombBuffer2[readPos2];
        mCombBuffer2[mCombPos2] = input + comb2 * combDecays[1];
        mCombPos2 = (mCombPos2 + 1) % kReverbBufferSize;
        combOut += comb2;
        
        // Comb 3
        int readPos3 = (mCombPos3 - combDelays[2] + kReverbBufferSize) % kReverbBufferSize;
        float comb3 = mCombBuffer3[readPos3];
        mCombBuffer3[mCombPos3] = input + comb3 * combDecays[2];
        mCombPos3 = (mCombPos3 + 1) % kReverbBufferSize;
        combOut += comb3;
        
        // Comb 4
        int readPos4 = (mCombPos4 - combDelays[3] + kReverbBufferSize) % kReverbBufferSize;
        float comb4 = mCombBuffer4[readPos4];
        mCombBuffer4[mCombPos4] = input + comb4 * combDecays[3];
        mCombPos4 = (mCombPos4 + 1) % kReverbBufferSize;
        combOut += comb4;
        
        combOut *= 0.25f;  // Average comb outputs
        
        // 2 Series Allpass Filters
        // Allpass 1
        int ap1ReadPos = (mAllpassPos1 - allpassDelays[0] + kReverbBufferSize) % kReverbBufferSize;
        float ap1Delayed = mAllpassBuffer1[ap1ReadPos];
        float ap1Out = ap1Delayed - allpassGain * combOut;
        mAllpassBuffer1[mAllpassPos1] = combOut + allpassGain * ap1Out;
        mAllpassPos1 = (mAllpassPos1 + 1) % kReverbBufferSize;
        
        // Allpass 2
        int ap2ReadPos = (mAllpassPos2 - allpassDelays[1] + kReverbBufferSize) % kReverbBufferSize;
        float ap2Delayed = mAllpassBuffer2[ap2ReadPos];
        float ap2Out = ap2Delayed - allpassGain * ap1Out;
        mAllpassBuffer2[mAllpassPos2] = ap1Out + allpassGain * ap2Out;
        mAllpassPos2 = (mAllpassPos2 + 1) % kReverbBufferSize;
        
        float reverbOut = ap2Out;
        
        // Mix wet and dry signals
        for (int32_t ch = 0; ch < channelCount; ch++) {
            int idx = i * channelCount + ch;
            buffer[idx] = buffer[idx] * dryMix + reverbOut * wetMix;
        }
    }
}


} // namespace euphoriae
