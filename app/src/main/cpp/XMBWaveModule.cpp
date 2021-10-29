//
// Created by EmiyaSyahriel on 20/05/2021.
//
#include <jni.h>
#include <android/asset_manager_jni.h>
#include "wave/typedefs.h"
#include "wave/states.h"
#include "wave/wave_gl.h"

extern "C" JNIEXPORT void JNICALL Java_id_psw_vshlauncher_livewallpaper_NativeGL_setMode(JNIEnv *env, jobject obj, jbyte mode){
    wave_type = static_cast<WAVE_TYPE>(mode);
}

extern "C" JNIEXPORT void JNICALL Java_id_psw_vshlauncher_livewallpaper_NativeGL_create(JNIEnv *env, jobject obj){
        wave_start();
    }

extern "C" JNIEXPORT void JNICALL Java_id_psw_vshlauncher_livewallpaper_NativeGL_destroy(JNIEnv *env, jobject obj){
        wave_kill();
    }

extern "C" JNIEXPORT void JNICALL Java_id_psw_vshlauncher_livewallpaper_NativeGL_setAssetManager(JNIEnv* env, jobject obj, jobject mgr){
        // wave_assetman = AAssetManager_fromJava(env, mgr);
        Log_w("AssetManager loaded from pointer %p", mgr)
    }

extern "C" JNIEXPORT void JNICALL Java_id_psw_vshlauncher_livewallpaper_NativeGL_draw(JNIEnv *env, jobject obj, jfloat deltaTime){
        wave_draw(deltaTime);
    }

extern "C" JNIEXPORT void JNICALL Java_id_psw_vshlauncher_livewallpaper_NativeGL_setup(JNIEnv *env, jobject obj, jint w, jint h){
        wave_resize(w,h);
    }

extern "C" JNIEXPORT void JNICALL
    Java_id_psw_vshlauncher_livewallpaper_NativeGL_setPaused(JNIEnv *env, jobject thiz, jboolean paused) {
        wave_paused = paused;
    }

extern "C" JNIEXPORT jboolean JNICALL Java_id_psw_vshlauncher_livewallpaper_NativeGL_getPaused(JNIEnv *env, jobject thiz) { return wave_paused; }