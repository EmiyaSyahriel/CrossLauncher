/**
 * Created by EmiyaSyahriel,  02:36 AM 15/04/2022
 * Live Wallpaper states implementation
 *
 * Why make it like this? Well, I want every variable to sit nicely with each other both in the
 * binary file and memory. Also sometimes gives a very small memory performance boosts, and
 * somehow produces smaller binary.
 *
 * Known IDE Problems:
 * - About that Clang-Tidy static init exception throw warning, It's guaranteed to be
 * super-super rare to happen since everything should have been initialized with less complex
 * allocation logics. Known cause is only because of system is running out of memory, where the
 * live wallpaper itself should not have to run anyway.
 *
 * - Visual Studio and VSCode IntelliSense sometimes complains about incompatible declaration,
 * just ignore it, This code conforms C Standard since the struct has name declared in the type.
 * And of course still compile-able by most compilers like GCC (on Unix), Android NDK Clang and
 * MSVC Compiler itself.
 */
#include "WAVE.HPP"

struct cxl_wave_state_t wave_state;

#ifdef EXTERN_PI
const double M_PI = 3.14159265358979323846;
#endif

#ifdef EXTERN_PIf
const float M_PIf = 3.141592f;
#endif

const struct cxl_wave_const_t wave_consts = { // NOLINT(cert-err58-cpp)
        64,
        256,
        6,
        { 0,1,2,1,3,2 },
        {
                -1.0f, -1.0f, 0.0f, 0.0f, // TL
                1.0f, -1.0f, 1.0f, 0.0f, // TR
                -1.0f,  1.0f, 0.0f, 1.0f, // BL
                1.0f,  1.0f, 1.0f, 1.0f, // BR
        },
        {
            "POSITION",
            "TEXCOORD0",
            "TEXCOORD1",
            "COLOR"
        },
        {
            "_Time","_TimeOfDay","_NormalStep",
            "_ColorA","_ColorB","_Ortho",
            "_RngDataA", "_RngDataB", "_RngTrans","_YScale"
        },{
                {
                        cxl_i2v4(0xB0B0B0FF),
                        cxl_i2v4(0xCA9E0AFF),
                        cxl_i2v4(0x7DA91EFF),
                        cxl_i2v4(0xE84A7BFF),
                        cxl_i2v4(0x137015FF),
                        cxl_i2v4(0x6855A4FF),
                        cxl_i2v4(0x137015FF),
                        cxl_i2v4(0x00074FFF),
                        cxl_i2v4(0xAB42C3FF),
                        cxl_i2v4(0xE49807FF),
                        cxl_i2v4(0x774917FF),
                        cxl_i2v4(0xD33C29FF)
                },
                {
                        cxl_i2v4(0xCCCCCCFF),
                        cxl_i2v4(0xD5DD32FF),
                        cxl_i2v4(0xA8B821FF),
                        cxl_i2v4(0xDC9CACFF),
                        cxl_i2v4(0x179A18FF),
                        cxl_i2v4(0xC282D9FF),
                        cxl_i2v4(0x0DE8E4FF),
                        cxl_i2v4(0x0AB0E0FF),
                        cxl_i2v4(0xCA5DDCFF),
                        cxl_i2v4(0xD7C501FF),
                        cxl_i2v4(0x98732DFF),
                        cxl_i2v4(0xE5432CFF)
                }
        },
        "cxl_wave.so",
    true,
        {
                {
                        "","Compilation","Linking"
                },
                {
                        "Invalid Enum", "Invalid Value", "Invalid Operation",
                        "Invalid Framebuffer Operation", "Video RAM Allocation Failed",
                        "OpenGL Unknown Error"
                },
                {
                        "GL_ERROR[%i] (%s) %s:%i ", "Shader %s error : %s"
                }
        }
};