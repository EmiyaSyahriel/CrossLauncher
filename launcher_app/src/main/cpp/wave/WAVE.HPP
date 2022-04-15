/**
 * Created by EmiyaSyahriel, 02:34 AM 15/04/2022
 * A one-for-all header file
 */
#ifndef XMBWAVE_H
#define XMBWAVE_H
// region Definitions
/** Buffer Size for Shader Validity Check */
#define SHADERCHECK_BUFSZ                   2048
/** Buffer Size for Shader Validity Check */
#define USED_BUFFER_COUNT                   16
#define RNG_BUFSZ                           16
#define RNG_DIVISION                        1024
#define RNG_RESEED_EVERY_N_CALL             1024

#ifdef ANDROID
#define INTERN_API __attribute__((visibility ("hidden")))
#define PUBLIC_API __attribute__((visibility ("default")))
#else
#define INTERN_API
#define PUBLIC_API
#endif
// endregion

// region Includes
#include <glm/vec2.hpp> // glm::vec2
#include <glm/vec3.hpp> // glm::vec3
#include <glm/vec4.hpp> // glm::vec4
#include <glm/mat4x4.hpp> // glm::mat4
#include <glm/ext/matrix_transform.hpp> // glm::translate, glm::rotate, glm::scale
#include <glm/ext/matrix_clip_space.hpp> // glm::perspective
#include <glm/ext/scalar_constants.hpp> // glm::pi
#include <vector> // std::vector

#if ANDROID
#include <GLES2/gl2.h> // Android OpenGL
#elif WIN32
#include "GL/glew.h" // OpenGL Wrangler for Desktop Platforms
#endif

#include "SHADERS.HPP" // Shader files

// endregion

// region Macros

#define lerp(a,b,t) ((a) + (((b) - (a)) * (t)))
#define arclerp(a,b,v) (((v) - (a)) / ((b) - (a)))
#define snrange(x) (((x) * 2) - 1)
#define unrange(x) (((x) + 1) / 2)

#ifndef M_PI
#define M_PI 3.14159265358979323846
#endif

#ifndef max
#define max(a,b) ((a) > (b) ? (a) : (b))
#endif

#ifndef min
#define min(a,b) ((a) < (b) ? (a) : (b))
#endif

/* Shift by Hex then divide it by 255 while also implicitly cast as float */
#define cxl_hexshift_f(c, shift) ((((c) >> ((shift) * 8)) & 0xFF) / 255.0f)
/* Converts color integer to glm::vec3 */
#define cxl_i2v3(c) glm::vec3(cxl_hexshift_f(c, 2), cxl_hexshift_f(c, 1),cxl_hexshift_f(c, 0))
/* Converts color integer to glm::vec4 (rgba) */
#define cxl_i2v4(c) glm::vec4(cxl_hexshift_f(c, 3), cxl_hexshift_f(c, 2), cxl_hexshift_f(c, 1),cxl_hexshift_f(c, 0))

#define HAS_FLAG(var, flag, t) (((t)(var) & (t)(flag)) == (t)(flag))

// endregion

// region Namespace Usages
using namespace std;
using namespace glm;
//endregion

// region Types and Typedefs
typedef const char* string_t;

typedef unsigned char byte;
typedef unsigned int uint;

enum class WAVE_STYLE : int8_t {
    PS3 = 0b0000,
    PSP = 0b0100,
    DEFAULT = 0b0000,
    PS3_NORMAL = 0b0000,
    PS3_BLINKS = 0b0010,
    PSP_BOTTOM = 0b0100,
    PSP_CENTER = 0b0110,
};
typedef struct {
    glm::vec2 pos_from, pos_to;
    float size_from, size_to;
    float alpha_from, alpha_to;
    float time, speed;
    glm::vec3 color;
} particle_t;

// region Static Structs
extern struct cxl_wave_state_t {
    WAVE_STYLE style, last_style;
    bool paused;
    float speed;
    int width, height;
    float current_time;
    struct cxl_colors_t{
        glm::vec3 bg_a, bg_b;
        glm::vec4 fg_a, fg_b;
    } colors;
    GLuint used_buffers[USED_BUFFER_COUNT];
    struct cxl_wave_bg_state_t {
        GLuint program, id, vt;
        GLint POSITION, TEXCOORD;
        GLint _ColorA,_ColorB,_TimeOfDay;
    } bg;
    struct cxl_wave_wave_3_state_t {
        GLuint program, id, vt;
        size_t idata_sz, vdata_sz;
        GLint _Time, _NormalStep, _Ortho, _ColorA,_ColorB;
        GLint _RngDataA, _RngDataB, _RngTrans;
        GLint POSITION;
    } ps3_wave;

    struct cxl_wave_wave_p_state_t {
        GLuint program, id, vt;
        size_t idata_sz, vdata_sz;
        GLint _Time, _Ortho, _ColorA, _ColorB;
        GLint _RngDataA, _RngDataB, _RngTrans;
        GLint POSITION, TEXCOORD1;
    } psp_wave;

    struct cxl_wave_spark_state_t {
        GLuint program, id, vt;
        GLint POSITION, COLOR;
        GLint _Ortho;
    } spark;
    struct cxl_rng_data_t{
        float A[16], B[16], t;
    } rng_data;
} wave_state;

extern const struct cxl_wave_const_t {
    int detail_size;
    int particle_count;
    int ring_count;
    GLuint bg_mdl_i[6];
    GLfloat bg_mdl_v[16];
    struct cxl_attr_name_t {
        string_t POSITION, TEXCOORD0, TEXCOORD1, COLOR;
    } attr_names ;
    struct cxl_unif_name_t {
        string_t _Time, _TimeOfDay, _NormalStep, _ColorA, _ColorB, _Ortho;
        string_t _RngDataA, _RngDataB, _RngTrans;
    } unif_names ;
    struct cxl_month_color_t {
        glm::vec3 top[12], bottom[12];
    } month_color;
    string_t tag;
    bool disable_glerr;
} wave_consts;

// endregion

// endregion

// region Logging System
#if ANDROID
#include <jni.h>
#include <android/log.h>

#define Log_i(...) __android_log_print(ANDROID_LOG_INFO, wave_consts.tag, __VA_ARGS__);
#define Log_d(...) __android_log_print(ANDROID_LOG_DEBUG, wave_consts.tag, __VA_ARGS__);
#define Log_e(...) __android_log_print(ANDROID_LOG_ERROR, wave_consts.tag, __VA_ARGS__);
#define Log_v(...) __android_log_print(ANDROID_LOG_VERBOSE, wave_consts.tag, __VA_ARGS__);
#define Log_w(...) __android_log_print(ANDROID_LOG_WARN, wave_consts.tag, __VA_ARGS__);
#define Log_f(...) __android_log_print(ANDROID_LOG_FATAL, wave_consts.tag, __VA_ARGS__);
#else

void INTERN_API W32_PRINT(char stype, string_t tag, string_t format, ...);

#define Log_i(...) W32_PRINT('I', wave_consts.tag, __VA_ARGS__);
#define Log_d(...) W32_PRINT('D', wave_consts.tag, __VA_ARGS__);
#define Log_e(...) W32_PRINT('E', wave_consts.tag, __VA_ARGS__);
#endif
// endregion

// region OpenGL Helper
mat4 INTERN_API cxl_wave_matrix();
void INTERN_API cxl_gl_check_error(int const line, const char* const file);
GLuint INTERN_API cxl_gl_compile_shader(const char* vertex, const char* fragment);
void INTERN_API cxl_gl_write_buffer(size_t const vsize, size_t const isize, GLuint const vbuffer,
                         GLuint const ibuffer, const GLfloat *vdata, const GLuint *idata,
                         GLenum drawmode);
void INTERN_API cxl_gl_reset_buffer();
void INTERN_API cxl_gl_enable_blend();
void INTERN_API cxl_gl_disable_blend();

#ifndef NDEBUG
#define CGL() cxl_gl_check_error(__LINE__, __FILE__)
#else
#define CGL() cxl_gl_check_error(0, wave_consts.tag)
#endif

// endregion

// region Time and RNG Helper
/** Get Time of Day */
float INTERN_API cxl_time_of_day();
float INTERN_API cxl_time_of_day_shader(float clock);
void INTERN_API cxl_buffered_rng(float* buffer, int count, int division = 1024, int maxSize = 1);
float INTERN_API cxl_get_rng();
// endregion

// region Sparkle Particle System
extern std::vector<particle_t> particles;
void INTERN_API cxl_spark_start();
void INTERN_API cxl_spark_draw(float ms);
// endregion

// region Background
void INTERN_API cxl_bg_start();
void INTERN_API cxl_bg_draw();
// endregion

// region PSP Wave
void INTERN_API cxl_psp_wave_start();
void INTERN_API cxl_psp_wave_draw(float ms);
// endregion

// region PS3 Wave
void INTERN_API cxl_ps3_wave_start();
void INTERN_API cxl_ps3_wave_draw(float ms);
// endregion

// region Main Entry point
void PUBLIC_API cxl_wave_start();
void PUBLIC_API cxl_wave_destroy();
void PUBLIC_API cxl_wave_draw(float ms);
void PUBLIC_API cxl_wave_resize(float w, float h);
// endregion

#endif