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
GLuint shader_bg, shader_wave, shader_sparkle;
GLuint vtbuff_bg, vtbuff_wave, vtbuff_sparkle;
GLuint idbuff_bg, idbuff_wave, idbuff_sparkle;
GLint vunif_wave_Time, vunif_wave_NormalStep, vattr_wave_Position, vattr_wave_VtxTimeA;
GLint vunif_wave_ColorA, vunif_wave_ColorB, vunif_wave_Ortho;
GLint vattr_spark_Position, vattr_spark_TexColor;
GLint vunif_spark_Ortho;
GLuint tex_day, tex_night;
int wave_index_size, wave_vdata_size;

GLfloat vtx_background_vdata[16] = {
        -1.0f, -1.0f, 0.0f, 0.0f, // TL
        1.0f, -1.0f, 1.0f, 0.0f, // TR
        -1.0f,  1.0f, 0.0f, 1.0f, // BL
        1.0f,  1.0f, 1.0f, 1.0f, // BR
};

GLuint vtx_background_index[6] = { 0,1,2,1,3,2 };

const char* const shaPosition = "POSITION";
const char* const shaTexCoord = "TEXCOORD0";
const char* const shaVtxTimeA = "TEXCOORD1";
const char* const shaVtxColor = "COLOR";
const char* const shuTime = "_Time";
const char* const shuTimeOfDay = "_TimeOfDay";
const char* const shuMonth = "_Month";
const char* const shuNormalStep = "_NormalStep";
const char* const shuColorA = "_ColorA";
const char* const shuColorB = "_ColorB";
const char* const shuColorC = "_ColorC";
const char* const shuOrtho = "_Ortho";