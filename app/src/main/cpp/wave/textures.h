#pragma once
#ifndef TEXTURES_H
#define TEXTURES_H
#include "gl.h"

extern const char siang_shades[], malam_shades[];
void load_texture(const char* which, GLuint* to, int size = 229, int width = 16, int height = 12);

#endif