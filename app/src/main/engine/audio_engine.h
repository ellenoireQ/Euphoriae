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

#ifndef EUPHORIAE_AUDIO_ENGINE_H
#define EUPHORIAE_AUDIO_ENGINE_H

#include <array>
#include <atomic>
#include <cmath>

namespace euphoriae {

/**
 * AudioEngine - Native audio effects processor
 */
class AudioEngine {
public:
    AudioEngine();
    ~AudioEngine() = default;

    // Process audio buffer in-place
    void processAudio(float* buffer, int32_t numFrames, int32_t channelCount);
    
    // ================== Effect Controls ==================
    
    // Basic effects
    void setVolume(float volume);
    void setBassBoost(float strength);
    void setVirtualizer(float strength);
    void setEqualizerBand(int band, float gainDb);
    
    // Advanced effects
    void setCompressor(float threshold, float ratio, float attack, float release);
    void setCompressorStrength(float strength);  // Simplified 0-1 control
    void setLimiter(float ceiling);
    void setSurround3D(float depth);
    void setRoomSize(float size);
    void setSurroundLevel(float level);  // Overall surround mix
    void setSurroundMode(int mode);      // 0=Off, 1=Music, 2=Movie, 3=Game, 4=Podcast
    void setHeadphoneSurround(bool enabled);  // Toggle headphone surround
    void setHeadphoneType(int type);  // 0=Generic, 1=InEar, 2=OverEar, 3=OpenBack, 4=Studio
    void setClarity(float level);
    void setTubeWarmth(float warmth);
    void setSpectrumExtension(float level);
    void setStereoBalance(float balance);  // -1 to 1
    void setChannelSeparation(float separation);
    void setTrebleBoost(float level);
    void setVolumeLeveler(float level);
    void setDynamicRange(float range);       // 0 to 1 (1 = full range)
    void setLoudnessGain(float gain);        // 0 to 1
    void setReverb(int preset, float wetMix);  // preset 0-6, wetMix 0-1
    
    // Time stretching / Pitch shifting
    void setTempo(float tempo);      // 0.5 to 2.0 (1.0 = normal)
    void setPitch(float semitones);  // -12 to +12 semitones
    float getTempo() const { return mTempo.load(); }
    float getPitch() const { return mPitchSemitones.load(); }
    
    // ================== Getters ==================
    
    float getVolume() const { return mVolume.load(); }
    float getBassBoost() const { return mBassBoost.load(); }
    float getVirtualizer() const { return mVirtualizer.load(); }
    float getCompressor() const { return mCompressorStrength.load(); }
    float getLimiter() const { return mLimiterCeiling.load(); }
    float getSurround3D() const { return mSurround3D.load(); }
    float getClarity() const { return mClarity.load(); }
    float getTubeWarmth() const { return mTubeWarmth.load(); }
    int getReverbPreset() const { return mReverbPreset.load(); }
    float getReverbWet() const { return mReverbWet.load(); }

private:
    // ================== Effect Processors ==================
    
    void applyBassBoost(float* buffer, int32_t numFrames, int32_t channelCount);
    void applyVirtualizer(float* buffer, int32_t numFrames, int32_t channelCount);
    void applyEqualizer(float* buffer, int32_t numFrames, int32_t channelCount);
    void applyCompressor(float* buffer, int32_t numFrames, int32_t channelCount);
    void applyLimiter(float* buffer, int32_t numSamples);
    void applySurround3D(float* buffer, int32_t numFrames);
    void applyClarity(float* buffer, int32_t numFrames, int32_t channelCount);
    void applyTubeWarmth(float* buffer, int32_t numSamples);
    void applySpectrumExtension(float* buffer, int32_t numFrames, int32_t channelCount);
    void applyStereoBalance(float* buffer, int32_t numFrames);
    void applyChannelSeparation(float* buffer, int32_t numFrames);
    void applyTrebleBoost(float* buffer, int32_t numFrames, int32_t channelCount);
    void applyVolumeLeveler(float* buffer, int32_t numFrames, int32_t channelCount);
    void applyReverb(float* buffer, int32_t numFrames, int32_t channelCount);
    void applyVolume(float* buffer, int32_t numSamples);

    // ================== Effect Parameters ==================
    
    // Basic
    std::atomic<float> mVolume{1.0f};
    std::atomic<float> mBassBoost{0.0f};
    std::atomic<float> mVirtualizer{0.0f};
    
    // Compressor
    std::atomic<float> mCompressorStrength{0.0f};
    std::atomic<float> mCompressorThreshold{-10.0f};  // dB
    std::atomic<float> mCompressorRatio{4.0f};
    std::atomic<float> mCompressorAttack{0.01f};  // seconds
    std::atomic<float> mCompressorRelease{0.1f};  // seconds
    
    // Limiter
    std::atomic<float> mLimiterCeiling{0.95f};
    
    // Surround/3D
    std::atomic<float> mSurround3D{0.0f};
    std::atomic<float> mRoomSize{0.5f};
    std::atomic<float> mSurroundLevel{0.5f};
    std::atomic<int> mSurroundMode{0};    // 0=Off, 1=Music, 2=Movie, 3=Game, 4=Podcast
    std::atomic<bool> mHeadphoneSurround{false};
    std::atomic<int> mHeadphoneType{0};  // 0=Generic, 1=InEar, 2=OverEar, 3=OpenBack, 4=Studio
    
    // Enhancement
    std::atomic<float> mClarity{0.0f};
    std::atomic<float> mTubeWarmth{0.0f};
    std::atomic<float> mSpectrumExtension{0.0f};
    std::atomic<float> mTrebleBoost{0.0f};
    std::atomic<float> mVolumeLeveler{0.0f};
    
    std::atomic<float> mStereoBalance{0.0f};
    std::atomic<float> mChannelSeparation{0.5f};
    std::atomic<float> mDynamicRange{1.0f};   // 0 to 1 (1 = full range, 0 = compressed)
    std::atomic<float> mLoudnessGain{0.0f};   // 0 to 1 (loudness enhancement)
    
    // Reverb
    std::atomic<int> mReverbPreset{0};  // 0=None, 1=SmallRoom, 2=MediumRoom, 3=LargeRoom, 4=MediumHall, 5=LargeHall, 6=Plate
    std::atomic<float> mReverbWet{0.0f};  // Wet/dry mix 0-1
    
    // Tempo/Pitch (WSOLA time stretching)
    std::atomic<float> mTempo{1.0f};          // 0.5 to 2.0
    std::atomic<float> mPitchSemitones{0.0f}; // -12 to +12
    float mPitchRatio{1.0f};                  // Calculated from semitones
    
    // WSOLA buffer for time stretching
    static constexpr int kWsolaBufferSize = 8192;
    static constexpr int kWsolaWindowSize = 1024;
    static constexpr int kWsolaOverlap = 256;
    float mWsolaBuffer[kWsolaBufferSize] = {0};
    int mWsolaWritePos = 0;
    int mWsolaReadPos = 0;
    float mWsolaPhase = 0.0f;
    
    // ================== Filter States ==================
    
    // Equalizer
    static constexpr int kNumEqualizerBands = 10;
    std::array<std::atomic<float>, kNumEqualizerBands> mEqualizerBands{};
    
    // Bass boost filter state (per channel)
    float mBassState[2] = {0.0f, 0.0f};
    
    // Biquad filter structure
    struct BiquadState {
        float z1 = 0.0f;
        float z2 = 0.0f;
    };
    std::array<BiquadState, kNumEqualizerBands * 2> mEqStates{}; // stereo
    
    // Clarity high-shelf filter state
    float mClarityState[2] = {0.0f, 0.0f};
    
    // Treble boost filter state
    float mTrebleState[2] = {0.0f, 0.0f};
    
    // Compressor envelope follower
    float mCompressorEnvelope = 0.0f;
    
    // Volume leveler RMS tracking
    float mRmsLevel = 0.0f;
    float mTargetRms = 0.3f;  // Target RMS level
    
    // 3D Surround delay buffer (for Haas effect)
    static constexpr int kMaxDelayFrames = 2048;
    float mDelayBufferL[kMaxDelayFrames] = {0};
    float mDelayBufferR[kMaxDelayFrames] = {0};
    int mDelayWritePos = 0;
    
    // Spectrum extension harmonic state
    float mHarmonicState[2] = {0.0f, 0.0f};
    
    // Reverb delay buffers (Schroeder reverb with 4 comb + 2 allpass filters)
    static constexpr int kReverbBufferSize = 8192;
    float mCombBuffer1[kReverbBufferSize] = {0};
    float mCombBuffer2[kReverbBufferSize] = {0};
    float mCombBuffer3[kReverbBufferSize] = {0};
    float mCombBuffer4[kReverbBufferSize] = {0};
    float mAllpassBuffer1[kReverbBufferSize] = {0};
    float mAllpassBuffer2[kReverbBufferSize] = {0};
    int mCombPos1 = 0, mCombPos2 = 0, mCombPos3 = 0, mCombPos4 = 0;
    int mAllpassPos1 = 0, mAllpassPos2 = 0;
};

} // namespace euphoriae


#endif // EUPHORIAE_AUDIO_ENGINE_H
