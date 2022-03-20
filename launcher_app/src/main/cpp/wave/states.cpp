#include "states.h"

WAVE_TYPE wave_type = WAVE_TYPE::PS3_BLINKS;
bool wave_paused;

int xmb_detail_size = 32;
float xmb_wave_speed = 1.0f;

int xmb_screen_w = 1280, xmb_screen_h = 720;
const int xmb_refscr_w = 1280, xmb_refscr_h = 720;
int xmb_particle_count = 256;
int xmb_particle_ring_count = 12;
glm::vec4 background_color_top, background_color_bottom;
glm::vec4 foreground_color_edge, foreground_color_center;

float currentTime = 0.0f;

GLint vunif_bg_ColorA, vunif_bg_ColorB, vunif_bg_ColorC, vattr_bg_Position, vattr_bg_TexCoord, vunif_bg_TimeOfDay, vunif_bg_Month;

buf_storage bg, wave, sparkle;

struct vunif_dat_bg  bg_unif;
struct vattr_dat_bg bg_attr;
struct vunif_dat_wave  wave_unif;
struct vattr_dat_wave  wave_attr;
struct vunif_dat_spark spark_unif ;
struct vattr_dat_spark spark_attr;

GLuint tex_day, tex_night;
int wave_index_size, wave_vdata_size;

GLfloat vtx_background_vdata[16] = {
        -1.0f, -1.0f, 0.0f, 0.0f, // TL
        1.0f, -1.0f, 1.0f, 0.0f, // TR
        -1.0f,  1.0f, 0.0f, 1.0f, // BL
        1.0f,  1.0f, 1.0f, 1.0f, // BR
};

GLuint vtx_background_index[6] = { 0,1,2,1,3,2 };

struct sha_text shader_attr_name {
        "POSITION",
        "TEXCOORD0",
        "TEXCOORD1",
        "COLOR"
};

struct shu_text shader_unif_name {
        "_Time",
        "_TimeOfDay",
        "_Month",
        "_NormalStep",
        "_ColorA",
        "_ColorB",
        "_ColorC",
        "_Ortho" ,
};