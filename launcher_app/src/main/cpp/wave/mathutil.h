//
// Created by ainaa on 17/11/2021.
//
#pragma once
#ifndef H_MATHUTIL
#define H_MATHUTIL
#include "typedefs.h"

#ifndef lerp
#define lerp(a,b,t) (a + ((b - a) * t))
#endif

#ifndef arclerp
#define arclerp(a,b,v) ((v - a) / (b - a))
#endif

#ifndef snrange
#define snrange(x) ((x * 2) - 1)
#endif

#ifndef unrange
#define unrange(x) ((x + 1) / 2)
#endif

#ifndef M_PI
#define M_PI 3.14159265358979323846
#endif

#ifndef getbyte
#define getbyte(i, shift) ((i >> shift) & 0xFF)
#endif

#ifndef int2color
#define int2color(i) glm::vec4(getbyte(i,16) / 255.0f,getbyte(i,8) / 255.0f,getbyte(i,0) / 255.0f,getbyte(i,24) / 255.0f)
#endif

#ifndef max
#define max(a,b) (a > b ? a : b)
#endif

#ifndef min
#define min(a,b) (a < b ? a : b)
#endif

float timeofday();
float timeofday_shader(float clock);
int monthofday();
glm::vec2 get_month_uv(int month);

#endif