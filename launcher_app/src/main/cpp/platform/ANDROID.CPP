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

EXTC JNIEXPORT void JNICALL Java_id_psw_vshlauncher_livewallpaper_NativeGL_setBackgroundMonth(JNIEnv *env, jobject obj, jbyte mode){
    wave_state.bg_month = (MONTH_COLOR_INDEX)mode;
}

EXTC JNIEXPORT void JNICALL Java_id_psw_vshlauncher_livewallpaper_NativeGL_setSpeed(JNIEnv *env, jobject obj, jfloat speed){
    wave_state.speed = speed;
}

EXTC JNIEXPORT void JNICALL Java_id_psw_vshlauncher_livewallpaper_NativeGL_setBackgroundColor(JNIEnv *env, jobject obj, jint top, jint bottom){
    vec4 a = cxl_i2v4(top), b = cxl_i2v4(bottom);
    wave_state.colors.bg_a = vec3(b.y, b.z, b.w);
    wave_state.colors.bg_b = vec3(a.y, a.z, a.w);
}

EXTC JNIEXPORT void JNICALL Java_id_psw_vshlauncher_livewallpaper_NativeGL_setForegroundColor(JNIEnv *env, jobject obj, jint edge, jint center){
    wave_state.colors.fg_a = cxl_i2v4a(edge);
    wave_state.colors.fg_b = cxl_i2v4a(center);
}

EXTC JNIEXPORT void JNICALL Java_id_psw_vshlauncher_livewallpaper_NativeGL_setVerticalScale(JNIEnv* env, jobject obj, jfloat scale){
    wave_state.vert_scale = scale;
}
EXTC JNIEXPORT void JNICALL Java_id_psw_vshlauncher_livewallpaper_NativeGL_setBgDayNightMode(JNIEnv *env, jobject thiz, jboolean dayNight) {
    wave_state.day_night = dayNight;
}