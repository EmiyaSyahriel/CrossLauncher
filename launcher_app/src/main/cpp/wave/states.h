#pragma once
#ifndef H_STATES
#define H_STATES

#include "typedefs.h"
#include "gl.h"

extern WAVE_TYPE wave_type;
extern bool wave_paused;
extern int xmb_detail_size;
extern float xmb_wave_speed;
extern int xmb_screen_w, xmb_screen_h;
extern const int xmb_refscr_w, xmb_refscr_h;
extern int xmb_particle_count;
extern int xmb_particle_ring_count;
extern glm::vec4 background_color_top, background_color_bottom;
extern glm::vec4 foreground_color_edge, foreground_color_center;
extern GLuint tex_day, tex_night;
extern float currentTime;

extern GLint vunif_bg_ColorA, vunif_bg_ColorB, vunif_bg_ColorC, vattr_bg_Position, vattr_bg_TexCoord, vunif_bg_TimeOfDay, vunif_bg_Month;

extern GLuint shader_bg, shader_wave, shader_sparkle;
extern GLuint vtbuff_bg, vtbuff_wave, vtbuff_sparkle;
extern GLuint idbuff_bg, idbuff_wave, idbuff_sparkle;

extern GLint vunif_wave_Time, vunif_wave_NormalStep, vattr_wave_Position, vattr_wave_VtxTimeA;
extern GLint vunif_wave_ColorA, vunif_wave_ColorB, vunif_wave_Ortho;
extern GLint vattr_spark_Position, vattr_spark_TexColor;
extern GLint vunif_spark_Ortho;

extern GLfloat vtx_background_vdata[16];
extern GLuint vtx_background_index[6];

extern int wave_index_size, wave_vdata_size;

extern const char* const shaPosition;
extern const char* const shaTexCoord;
extern const char* const shaVtxTimeA;
extern const char* const shaVtxColor;
extern const char* const shuTime;
extern const char* const shuTimeOfDay;
extern const char* const shuMonth;
extern const char* const shuNormalStep;
extern const char* const shuColorA;
extern const char* const shuColorB;
extern const char* const shuColorC;
extern const char* const shuOrtho;

#endif