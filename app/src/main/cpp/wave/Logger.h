
//
// Created by EmiyaSyahriel on 20/05/2021.
//
#pragma once

#define WAVE_TAG "lines.qrc"

#if ANDROID
#include <jni.h>
#include <android/log.h>

#define Log_i(...) __android_log_print(ANDROID_LOG_INFO, WAVE_TAG, __VA_ARGS__);
#define Log_d(...) __android_log_print(ANDROID_LOG_DEBUG, WAVE_TAG, __VA_ARGS__);
#define Log_e(...) __android_log_print(ANDROID_LOG_ERROR, WAVE_TAG, __VA_ARGS__);
#define Log_v(...) __android_log_print(ANDROID_LOG_VERBOSE, WAVE_TAG, __VA_ARGS__);
#define Log_w(...) __android_log_print(ANDROID_LOG_WARN, WAVE_TAG, __VA_ARGS__);
#define Log_f(...) __android_log_print(ANDROID_LOG_FATAL, WAVE_TAG, __VA_ARGS__);
#endif

#if WIN32

void W32_PRINT(char stype, char* tag, char* format, ...);

#define Log_i(...) W32_PRINT('I', WAVE_TAG, __VA_ARGS__);
#define Log_e(...) W32_PRINT('E', WAVE_TAG, __VA_ARGS__);
#endif