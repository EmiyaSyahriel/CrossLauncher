#ifndef H_PS3_WAVE
#define H_PS3_WAVE
#include "states.h"
#include "typedefs.h"

void ps3_compile_shaders();
void ps3_generate_buffers();
void ps3_onresize();
void ps3_start();
void ps3_draw();
void ps3_destroy();

#endif