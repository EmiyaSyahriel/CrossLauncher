#include "states.h"

WAVE_TYPE wave_type = WAVE_TYPE::PS3_BLINKS;
bool wave_paused;

int xmb_detail_size = 32;
float xmb_wave_speed = 1.0f;

int xmb_screen_w = 1280, xmb_screen_h = 720;
const int xmb_refscr_w = 1280, xmb_refscr_h = 720;
int xmb_particle_count = 256;
int xmb_particle_ring_count = 12;