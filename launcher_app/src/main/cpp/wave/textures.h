#pragma once
#ifndef TEXTURES_H
#define TEXTURES_H
#include "gl.h"

extern const char siang_shades[], malam_shades[];
void load_texture(const char* which, GLuint* to, int size = 192, int width = 48, int height = 4);

#endif