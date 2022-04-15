/**
 * Created by EmiyaSyahriel, 20/05/2021.
 * JNI Exports for use in Android and possibly LWJGL-based Java applications
 */
#include <jni.h>
#include "../wave/WAVE.HPP"

#define EXTC extern "C"

EXTC JNIEXPORT void JNICALL Java_id_psw_vshlauncher_livewallpaper_NativeGL_create(JNIEnv *env, jobject obj){
        cxl_wave_start();
    }

EXTC JNIEXPORT void JNICALL Java_id_psw_vshlauncher_livewallpaper_NativeGL_destroy(JNIEnv *env, jobject obj){
        cxl_wave_destroy();
    }

EXTC JNIEXPORT void JNICALL Java_id_psw_vshlauncher_livewallpaper_NativeGL_draw(JNIEnv *env, jobject obj, jfloat deltaTime){
        cxl_wave_draw(deltaTime);
    }

EXTC JNIEXPORT void JNICALL Java_id_psw_vshlauncher_livewallpaper_NativeGL_setup(JNIEnv *env, jobject obj, jint w, jint h){
        cxl_wave_resize(w,h);
    }

EXTC JNIEXPORT void JNICALL
    Java_id_psw_vshlauncher_livewallpaper_NativeGL_setPaused(JNIEnv *env, jobject thiz, jboolean paused) {
        wave_state.paused = paused;
    }

EXTC JNIEXPORT jboolean JNICALL Java_id_psw_vshlauncher_livewallpaper_NativeGL_getPaused(JNIEnv *env, jobject thiz) { return wave_state.paused; }

EXTC JNIEXPORT void JNICALL Java_id_psw_vshlauncher_livewallpaper_NativeGL_setWaveStyle(JNIEnv *env, jobject obj, jbyte mode){
    wave_state.style = static_cast<WAVE_STYLE>(mode);
}

EXTC JNIEXPORT void JNICALL Java_id_psw_vshlauncher_livewallpaper_NativeGL_setSpeed(JNIEnv *env, jobject obj, jfloat speed){
    wave_state.speed = speed;
}

EXTC JNIEXPORT void JNICALL Java_id_psw_vshlauncher_livewallpaper_NativeGL_setBackgroundColor(JNIEnv *env, jobject obj, jint top, jint bottom){
    wave_state.colors.bg_a = cxl_i2v4(top);
    wave_state.colors.bg_b = cxl_i2v4(bottom);
}

EXTC JNIEXPORT void JNICALL Java_id_psw_vshlauncher_livewallpaper_NativeGL_setForegroundColor(JNIEnv *env, jobject obj, jint edge, jint center){
    wave_state.colors.fg_a = cxl_i2v4(edge);
    wave_state.colors.fg_b = cxl_i2v4(center);
}