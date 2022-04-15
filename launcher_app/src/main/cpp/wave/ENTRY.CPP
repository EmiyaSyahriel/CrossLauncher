/**
 * Created by EmiyaSyahriel, 05:36 AM 15/04/2022
 * Callback Entries for Live Wave Wallpaper Functionalities
 */

#include "WAVE.HPP"
#include <string>

#define SET_BUFFER(what, start) \
wave_state. what .id = wave_state.used_buffers[(start * 2) + 0];\
wave_state. what .vt = wave_state.used_buffers[(start * 2) + 1]

void cxl_wave_start(){
    wave_state.bg.program = cxl_gl_compile_shader(R::ps3_background_vert, R::ps3_background_frag);
    wave_state.ps3_wave.program = cxl_gl_compile_shader( R::ps3_wave_vert, R::ps3_wave_frag);
    wave_state.spark.program = cxl_gl_compile_shader(R::ps3_sparkle_vert, R::ps3_sparkle_frag);
    wave_state.psp_wave.program = cxl_gl_compile_shader( R::psp_wave_vert, R::psp_wave_frag);

    glGenBuffers(USED_BUFFER_COUNT, wave_state.used_buffers);
    SET_BUFFER(bg, 0);
    SET_BUFFER(ps3_wave, 1);
    SET_BUFFER(spark, 2);
    SET_BUFFER(psp_wave, 3);

    cxl_buffered_rng(wave_state.rng_data.A, 16, 1024, 3);
    cxl_buffered_rng(wave_state.rng_data.B, 16, 1024, 3);
    wave_state.rng_data.t = 0.0f;

    cxl_bg_start();
    cxl_ps3_wave_start();
    cxl_psp_wave_start();
    cxl_spark_start();
}

void cxl_wave_destroy(){
    glDeleteBuffers(USED_BUFFER_COUNT, wave_state.used_buffers);
    glDeleteProgram(wave_state.bg.program);
    glDeleteProgram(wave_state.spark.program);
    glDeleteProgram(wave_state.ps3_wave.program);
    glDeleteProgram(wave_state.psp_wave.program);
}

void cxl_wave_draw(float ms){
    if(wave_state.paused) return;

    wave_state.rng_data.t += ms / 30.0f;
    if(wave_state.rng_data.t >= 1.0f){
        memcpy(wave_state.rng_data.A, wave_state.rng_data.B, sizeof(float) * RNG_BUFSZ);
        cxl_buffered_rng(wave_state.rng_data.B, RNG_BUFSZ, RNG_DIVISION, 1);
        wave_state.rng_data.t = 0.0f;
    }

    wave_state.current_time += ms;
    if (wave_state.current_time >= 100000.0f) wave_state.current_time = 0.0f;

    bool isPSP = HAS_FLAG(wave_state.style, WAVE_STYLE::PSP, int8_t);
    cxl_gl_enable_blend();
    cxl_bg_draw();
    if (!isPSP) {
        cxl_ps3_wave_draw(ms);
        if (HAS_FLAG(wave_state.style, WAVE_STYLE::PS3_BLINKS, int8_t)) {
            cxl_spark_draw(ms);
        }
    }
    else {
        cxl_psp_wave_draw(ms);
    }
    cxl_gl_disable_blend();
}

void cxl_wave_resize(float w, float h){
    glViewport(0,0, (int)w, (int)h);
    wave_state.width = w;
    wave_state.height = h;
}

