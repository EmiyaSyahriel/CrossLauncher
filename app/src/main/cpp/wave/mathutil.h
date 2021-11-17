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

#endif