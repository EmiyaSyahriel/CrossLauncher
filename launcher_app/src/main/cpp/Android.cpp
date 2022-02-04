//
// Created by EmiyaSyahriel on 20/05/2021.
//
#include <jni.h>
#include "wave/typedefs.h"
#include "wave/states.h"
#include "wave/mathutil.h"
#include "wave/wave.h"

#ifdef __cplusplus
#define EXTC extern "C"
#else
#define EXTC
#endif

EXTC JNIEXPORT void JNICALL Java_id_psw_vshlauncher_livewallpaper_NativeGL_create(JNIEnv *env, jobject obj){
        wave_start();
    }

EXTC JNIEXPORT void JNICALL Java_id_psw_vshlauncher_livewallpaper_NativeGL_destroy(JNIEnv *env, jobject obj){
        wave_destroy();
    }

EXTC JNIEXPORT void JNICALL Java_id_psw_vshlauncher_livewallpaper_NativeGL_draw(JNIEnv *env, jobject obj, jfloat deltaTime){
        wave_draw(deltaTime);
    }

EXTC JNIEXPORT void JNICALL Java_id_psw_vshlauncher_livewallpaper_NativeGL_setup(JNIEnv *env, jobject obj, jint w, jint h){
        wave_resize(w,h);
    }

EXTC JNIEXPORT void JNICALL
    Java_id_psw_vshlauncher_livewallpaper_NativeGL_setPaused(JNIEnv *env, jobject thiz, jboolean paused) {
        wave_paused = paused;
    }

EXTC JNIEXPORT jboolean JNICALL Java_id_psw_vshlauncher_livewallpaper_NativeGL_getPaused(JNIEnv *env, jobject thiz) { return wave_paused; }

EXTC JNIEXPORT void JNICALL Java_id_psw_vshlauncher_livewallpaper_NativeGL_setWaveStyle(JNIEnv *env, jobject obj, jbyte mode){
    wave_type = static_cast<WAVE_TYPE>(mode);
}

EXTC JNIEXPORT void JNICALL Java_id_psw_vshlauncher_livewallpaper_NativeGL_setSpeed(JNIEnv *env, jobject obj, jfloat speed){
    xmb_wave_speed = speed;
}

EXTC JNIEXPORT void JNICALL Java_id_psw_vshlauncher_livewallpaper_NativeGL_setBackgroundColor(JNIEnv *env, jobject obj, jint top, jint bottom){
    background_color_top = int2color(top);
    background_color_bottom = int2color(bottom);
}

EXTC JNIEXPORT void JNICALL Java_id_psw_vshlauncher_livewallpaper_NativeGL_setForegroundColor(JNIEnv *env, jobject obj, jint edge, jint center){
    foreground_color_edge = int2color(edge);
    foreground_color_center = int2color(center);
}