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
            "_RngDataA", "_RngDataB", "_RngTrans"
        },{
                {
                        cxl_i2v4(0xFFB0B0B0),
                        cxl_i2v4(0xFFCA9E0A),
                        cxl_i2v4(0xFF7DA91E),
                        cxl_i2v4(0xFFE84A7B),
                        cxl_i2v4(0xFF137015),
                        cxl_i2v4(0xFF6855A4),
                        cxl_i2v4(0xFF137015),
                        cxl_i2v4(0xFF00074F),
                        cxl_i2v4(0xFFAB42C3),
                        cxl_i2v4(0xFFE49807),
                        cxl_i2v4(0xFF774917),
                        cxl_i2v4(0xFFD33C29)
                },
                {
                        cxl_i2v4(0xFFCCCCCC),
                        cxl_i2v4(0xFFD5DD32),
                        cxl_i2v4(0xFFA8B821),
                        cxl_i2v4(0xFFDC9CAC),
                        cxl_i2v4(0xFF179A18),
                        cxl_i2v4(0xFFC282D9),
                        cxl_i2v4(0xFF0DE8E4),
                        cxl_i2v4(0xFF0AB0E0),
                        cxl_i2v4(0xFFCA5DDC),
                        cxl_i2v4(0xFFD7C501),
                        cxl_i2v4(0xFF98732D),
                        cxl_i2v4(0xFFE5432C)
                }
        },
        "cxl_wave.so",
    true
};