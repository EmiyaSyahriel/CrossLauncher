//
// Created by ainaa on 17/11/2021.
//

#include "wave.h"
#include "ps3_wave.h"
#include "psp_wave.h"
#include "states.h"
#include "typedefs.h"

#define COMMAND_TYPE_SELECT(_type_, command_a, command_b, call) if(HAS_FLAG(wave_type, WAVE_TYPE::_type_, int8_t)) \
command_a call; else command_b call

void wave_start(){
    COMMAND_TYPE_SELECT(PSP, psp_start, ps3_start, ());
}

void wave_destroy(){
    COMMAND_TYPE_SELECT(PSP, psp_destroy, ps3_destroy, ());
}

void wave_draw(float ms){
    if(wave_paused) return;
    COMMAND_TYPE_SELECT(PSP, psp_draw, ps3_draw, (ms));
}

void wave_resize(float w, float h){
    COMMAND_TYPE_SELECT(PSP, psp_resize, ps3_resize, (w,h));
}


