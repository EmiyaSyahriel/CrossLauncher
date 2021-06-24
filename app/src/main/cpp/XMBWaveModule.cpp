//
// Created by EmiyaSyahriel on 20/05/2021.
//
#include <jni.h>
#include <android/asset_manager_jni.h>
#include "wave/typedefs.h"
#include "wave/GLState.h"

extern "C" JNIEXPORT void JNICALL Java_id_psw_vshlauncher_livewallpaper_NativeGL_create(JNIEnv *env, jobject obj){
        GLState::create();
    }

extern "C" JNIEXPORT void JNICALL Java_id_psw_vshlauncher_livewallpaper_NativeGL_destroy(JNIEnv *env, jobject obj){
        // GLState::kill();
    }

extern "C" JNIEXPORT void JNICALL Java_id_psw_vshlauncher_livewallpaper_NativeGL_setAssetManager(JNIEnv* env, jobject obj, jobject mgr){
        GLState::assetmgr = AAssetManager_fromJava(env, mgr);
    }

extern "C" JNIEXPORT void JNICALL Java_id_psw_vshlauncher_livewallpaper_NativeGL_draw(JNIEnv *env, jobject obj, jfloat deltaTime){
        GLState::render(deltaTime);
    }

extern "C" JNIEXPORT void JNICALL Java_id_psw_vshlauncher_livewallpaper_NativeGL_setup(JNIEnv *env, jobject obj, jint w, jint h){
        GLState::setup(w,h);
    }

extern "C" JNIEXPORT void JNICALL
    Java_id_psw_vshlauncher_livewallpaper_NativeGL_setPaused(JNIEnv *env, jobject thiz, jboolean paused) {
        GLState::pause(paused);
    }

extern "C" JNIEXPORT jboolean JNICALL Java_id_psw_vshlauncher_livewallpaper_NativeGL_getPaused(JNIEnv *env, jobject thiz) { return GLState::bGetIsPaused(); }