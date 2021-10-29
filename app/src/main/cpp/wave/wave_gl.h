#pragma once
#ifndef H_WAVE_GL
#define H_WAVE_GL

#include "Logger.h"
#include <cstdlib>

void wave_start();
void wave_kill();
void wave_compile(uint prog, char* sh_path);
void wave_link(uint prog, uint vs, uint fs);
void wave_resize(int w, int h);
extern bool wave_paused;
void wave_ensure_needed_data();
void wave_draw(float ms);

#endif // HH_WAVE_GL