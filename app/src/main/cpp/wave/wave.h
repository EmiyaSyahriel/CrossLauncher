//
// Created by ainaa on 17/11/2021.
//

#pragma once
#ifndef H_WAVE_HUB
#define H_WAVE_HUB

#include "Logger.h"

void wave_start();
void wave_destroy();
void wave_draw(float ms);
void wave_resize(float w, float h);

#endif // H_WAVE_HUB