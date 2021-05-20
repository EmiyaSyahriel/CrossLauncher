//
// Created by EmiyaSyahriel on 20/05/2021.
//
#include <jni.h>
#include <android/asset_manager_jni.h>
#include "wave/typedefs.h"
#include "wave/GLRenderer.h"
#include "wave/GLState.h"

#ifdef __cplusplus
extern "C"{
#endif
    JNIEXPORT void JNICALL Java_id_psw_vshlauncher_livewallpaper_NativeGL_create(JNIEnv *env, jobject obj){
        GLState::create();
    }

    JNIEXPORT void JNICALL Java_id_psw_vshlauncher_livewallpaper_NativeGL_setAssetManager(JNIEnv* env, jobject obj, jobject mgr){
        GLState::assetmgr = AAssetManager_fromJava(env, mgr);
    }

    JNIEXPORT void JNICALL Java_id_psw_vshlauncher_livewallpaper_NativeGL_draw(JNIEnv *env, jobject obj, jfloat deltaTime){
        GLState::render(deltaTime);
    }

    JNIEXPORT void JNICALL Java_id_psw_vshlauncher_livewallpaper_NativeGL_setup(JNIEnv *env, jobject obj, jint w, jint h){
        GLState::setup(w,h);
    }

#ifdef __cplusplus
}
#endif