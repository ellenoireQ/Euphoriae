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

#include <jni.h>
#include "audio_engine.h"
#include <memory>
#include <android/log.h>

#define LOG_TAG "EuphoriaeAudio"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)

static std::unique_ptr<euphoriae::AudioEngine> sEngine;

extern "C" {

// ================== Core ==================

JNIEXPORT void JNICALL
Java_com_oss_euphoriae_engine_AudioEngine_nativeCreate(JNIEnv *env, jobject thiz) {
    if (!sEngine) {
        sEngine = std::make_unique<euphoriae::AudioEngine>();
        LOGI("Native AudioEngine instance created with full DSP");
    }
}

JNIEXPORT void JNICALL
Java_com_oss_euphoriae_engine_AudioEngine_nativeDestroy(JNIEnv *env, jobject thiz) {
    sEngine.reset();
    LOGI("Native AudioEngine instance destroyed");
}

JNIEXPORT void JNICALL
Java_com_oss_euphoriae_engine_AudioEngine_nativeProcessAudio(
        JNIEnv *env, 
        jobject thiz, 
        jfloatArray audioBuffer, 
        jint numFrames, 
        jint channelCount) {
    if (!sEngine || audioBuffer == nullptr) return;
    
    jfloat* buffer = env->GetFloatArrayElements(audioBuffer, nullptr);
    if (buffer == nullptr) return;
    
    sEngine->processAudio(buffer, numFrames, channelCount);
    
    env->ReleaseFloatArrayElements(audioBuffer, buffer, 0);
}

// ================== Basic Effects ==================

JNIEXPORT void JNICALL
Java_com_oss_euphoriae_engine_AudioEngine_nativeSetVolume(JNIEnv *env, jobject thiz, jfloat volume) {
    if (sEngine) sEngine->setVolume(volume);
}

JNIEXPORT void JNICALL
Java_com_oss_euphoriae_engine_AudioEngine_nativeSetBassBoost(JNIEnv *env, jobject thiz, jfloat strength) {
    if (sEngine) sEngine->setBassBoost(strength);
}

JNIEXPORT void JNICALL
Java_com_oss_euphoriae_engine_AudioEngine_nativeSetVirtualizer(JNIEnv *env, jobject thiz, jfloat strength) {
    if (sEngine) sEngine->setVirtualizer(strength);
}

JNIEXPORT void JNICALL
Java_com_oss_euphoriae_engine_AudioEngine_nativeSetEqualizerBand(JNIEnv *env, jobject thiz, jint band, jfloat gain) {
    if (sEngine) sEngine->setEqualizerBand(band, gain);
}

// ================== Advanced Effects ==================

JNIEXPORT void JNICALL
Java_com_oss_euphoriae_engine_AudioEngine_nativeSetCompressor(JNIEnv *env, jobject thiz, jfloat strength) {
    if (sEngine) sEngine->setCompressorStrength(strength);
}

JNIEXPORT void JNICALL
Java_com_oss_euphoriae_engine_AudioEngine_nativeSetLimiter(JNIEnv *env, jobject thiz, jfloat ceiling) {
    if (sEngine) sEngine->setLimiter(ceiling);
}

JNIEXPORT void JNICALL
Java_com_oss_euphoriae_engine_AudioEngine_nativeSetSurround3D(JNIEnv *env, jobject thiz, jfloat depth) {
    if (sEngine) sEngine->setSurround3D(depth);
}

JNIEXPORT void JNICALL
Java_com_oss_euphoriae_engine_AudioEngine_nativeSetRoomSize(JNIEnv *env, jobject thiz, jfloat size) {
    if (sEngine) sEngine->setRoomSize(size);
}

JNIEXPORT void JNICALL
Java_com_oss_euphoriae_engine_AudioEngine_nativeSetSurroundLevel(JNIEnv *env, jobject thiz, jfloat level) {
    if (sEngine) sEngine->setSurroundLevel(level);
}

JNIEXPORT void JNICALL
Java_com_oss_euphoriae_engine_AudioEngine_nativeSetSurroundMode(JNIEnv *env, jobject thiz, jint mode) {
    if (sEngine) sEngine->setSurroundMode(mode);
}

JNIEXPORT void JNICALL
Java_com_oss_euphoriae_engine_AudioEngine_nativeSetHeadphoneSurround(JNIEnv *env, jobject thiz, jboolean enabled) {
    if (sEngine) sEngine->setHeadphoneSurround(enabled);
}

JNIEXPORT void JNICALL
Java_com_oss_euphoriae_engine_AudioEngine_nativeSetHeadphoneType(JNIEnv *env, jobject thiz, jint type) {
    if (sEngine) sEngine->setHeadphoneType(type);
}

JNIEXPORT void JNICALL
Java_com_oss_euphoriae_engine_AudioEngine_nativeSetClarity(JNIEnv *env, jobject thiz, jfloat level) {
    if (sEngine) sEngine->setClarity(level);
}

JNIEXPORT void JNICALL
Java_com_oss_euphoriae_engine_AudioEngine_nativeSetTubeWarmth(JNIEnv *env, jobject thiz, jfloat warmth) {
    if (sEngine) sEngine->setTubeWarmth(warmth);
}

JNIEXPORT void JNICALL
Java_com_oss_euphoriae_engine_AudioEngine_nativeSetSpectrumExtension(JNIEnv *env, jobject thiz, jfloat level) {
    if (sEngine) sEngine->setSpectrumExtension(level);
}

JNIEXPORT void JNICALL
Java_com_oss_euphoriae_engine_AudioEngine_nativeSetTrebleBoost(JNIEnv *env, jobject thiz, jfloat level) {
    if (sEngine) sEngine->setTrebleBoost(level);
}

JNIEXPORT void JNICALL
Java_com_oss_euphoriae_engine_AudioEngine_nativeSetVolumeLeveler(JNIEnv *env, jobject thiz, jfloat level) {
    if (sEngine) sEngine->setVolumeLeveler(level);
}

// ================== Stereo ==================

JNIEXPORT void JNICALL
Java_com_oss_euphoriae_engine_AudioEngine_nativeSetStereoBalance(JNIEnv *env, jobject thiz, jfloat balance) {
    if (sEngine) sEngine->setStereoBalance(balance);
}

JNIEXPORT void JNICALL
Java_com_oss_euphoriae_engine_AudioEngine_nativeSetChannelSeparation(JNIEnv *env, jobject thiz, jfloat separation) {
    if (sEngine) sEngine->setChannelSeparation(separation);
}

JNIEXPORT void JNICALL
Java_com_oss_euphoriae_engine_AudioEngine_nativeSetDynamicRange(JNIEnv *env, jobject thiz, jfloat range) {
    if (sEngine) sEngine->setDynamicRange(range);
}

JNIEXPORT void JNICALL
Java_com_oss_euphoriae_engine_AudioEngine_nativeSetLoudnessGain(JNIEnv *env, jobject thiz, jfloat gain) {
    if (sEngine) sEngine->setLoudnessGain(gain);
}

// ================== Getters ==================

JNIEXPORT jfloat JNICALL
Java_com_oss_euphoriae_engine_AudioEngine_nativeGetVolume(JNIEnv *env, jobject thiz) {
    return sEngine ? sEngine->getVolume() : 1.0f;
}

JNIEXPORT jfloat JNICALL
Java_com_oss_euphoriae_engine_AudioEngine_nativeGetBassBoost(JNIEnv *env, jobject thiz) {
    return sEngine ? sEngine->getBassBoost() : 0.0f;
}

JNIEXPORT jfloat JNICALL
Java_com_oss_euphoriae_engine_AudioEngine_nativeGetVirtualizer(JNIEnv *env, jobject thiz) {
    return sEngine ? sEngine->getVirtualizer() : 0.0f;
}

JNIEXPORT jfloat JNICALL
Java_com_oss_euphoriae_engine_AudioEngine_nativeGetCompressor(JNIEnv *env, jobject thiz) {
    return sEngine ? sEngine->getCompressor() : 0.0f;
}

JNIEXPORT jfloat JNICALL
Java_com_oss_euphoriae_engine_AudioEngine_nativeGetClarity(JNIEnv *env, jobject thiz) {
    return sEngine ? sEngine->getClarity() : 0.0f;
}

JNIEXPORT jfloat JNICALL
Java_com_oss_euphoriae_engine_AudioEngine_nativeGetTubeWarmth(JNIEnv *env, jobject thiz) {
    return sEngine ? sEngine->getTubeWarmth() : 0.0f;
}

JNIEXPORT void JNICALL
Java_com_oss_euphoriae_engine_AudioEngine_nativeSetReverb(JNIEnv *env, jobject thiz, jint preset, jfloat wetMix) {
    if (sEngine) sEngine->setReverb(preset, wetMix);
}

JNIEXPORT jint JNICALL
Java_com_oss_euphoriae_engine_AudioEngine_nativeGetReverbPreset(JNIEnv *env, jobject thiz) {
    return sEngine ? sEngine->getReverbPreset() : 0;
}

// Tempo/Pitch
JNIEXPORT void JNICALL
Java_com_oss_euphoriae_engine_AudioEngine_nativeSetTempo(JNIEnv *env, jobject thiz, jfloat tempo) {
    if (sEngine) sEngine->setTempo(tempo);
}

JNIEXPORT void JNICALL
Java_com_oss_euphoriae_engine_AudioEngine_nativeSetPitch(JNIEnv *env, jobject thiz, jfloat semitones) {
    if (sEngine) sEngine->setPitch(semitones);
}

JNIEXPORT jfloat JNICALL
Java_com_oss_euphoriae_engine_AudioEngine_nativeGetTempo(JNIEnv *env, jobject thiz) {
    return sEngine ? sEngine->getTempo() : 1.0f;
}

JNIEXPORT jfloat JNICALL
Java_com_oss_euphoriae_engine_AudioEngine_nativeGetPitch(JNIEnv *env, jobject thiz) {
    return sEngine ? sEngine->getPitch() : 0.0f;
}

} // extern "C"

