//
// Created by ainaa on 17/11/2021.
//
#ifndef H_MATHUTIL
#define H_MATHUTIL
#pragma once

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

#endif