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

struct buf_storage {
    GLuint shader, vtbuf, idbuf;
};

extern buf_storage bg, wave, sparkle;

extern struct vunif_dat_bg{
    GLint colorA, colorB, colorC, timeOfDay, month;
} bg_unif;

extern struct vattr_dat_bg{
    GLint position, texCoord;
} bg_attr;

extern struct vunif_dat_wave {
    GLint time, normalStep;
    GLint colorA, colorB, ortho;
} wave_unif ;

extern struct vattr_dat_wave {
    GLint  position, vtxTimeA;
} wave_attr;

extern struct vunif_dat_spark {
    GLint ortho;
} spark_unif ;

extern struct vattr_dat_spark{
    GLint position, texColor;
} spark_attr;

extern GLfloat vtx_background_vdata[16];
extern GLuint vtx_background_index[6];

extern int wave_index_size, wave_vdata_size;

extern struct sha_text {
    const char* const position;
    const char* const texCoord;
    const char* const vtxTimeA;
    const char* const vtxColor;
} shader_attr_name;

extern struct shu_text{
    const char* const time;
    const char* const timeOfDay;
    const char* const month;
    const char* const normalStep;
    const char* const colorA;
    const char* const colorB;
    const char* const colorC;
    const char* const ortho;
} shader_unif_name;

#endif