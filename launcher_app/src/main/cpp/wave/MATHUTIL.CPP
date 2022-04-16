/**
 * Created by EmiyaSyahriel on 17/11/2021.
 * Mathematical, Time and Random Number Generation Utilities
 */

/**
 * Make Clang-Tidy ignores C Stdlib rand() and srand() usage. It's used as a cosmetic instead
 * of security, and it's allowed to be predictable anyway.
 */
#pragma ide diagnostic ignored "cert-msc51-cpp"
#pragma ide diagnostic ignored "cert-msc50-cpp"
#include "WAVE.HPP"
#include <chrono>
#include <cstdlib>

float cxl_time_of_day() {
	time_t now = time(nullptr);
	tm* today = localtime(&now);
	return (((float)today->tm_hour * 60) + (float)today->tm_min);
}

float cxl_time_of_day_shader(float clock) {
	float w = clock / 1440.0f;
	float retval;
	if (w >= 0.16f && w < 0.33f) retval = 1.0f - unrange(cos(arclerp(0.16f, 0.33f, w)));
	else if (w >= 0.33f && w < 0.66f) retval = 1.0f;
	else if (w >= 0.66f && w < 0.82f) retval = unrange(cos(arclerp(0.66f, 0.82f, w)));
	else retval = 0.0f;
	return retval;
}

int sRandCalled = 0;
void cxl_buffered_rng(float* buffer, int count, int division, int maxSize){
	for(int i =0 ; i < count ;i++){
		buffer[i] = ((float)(rand() % division) / (float)division) * (float)maxSize;
	}
	sRandCalled += count;
	if (sRandCalled >= RNG_RESEED_EVERY_N_CALL) {
		// Only reseed every several times
		srand(time(nullptr));
		sRandCalled = 0;
	}
}

float cxl_get_rng_buffer[RNG_BUFSZ];
int cxl_get_rng_buffi = 0;

float cxl_get_rng(){
	cxl_get_rng_buffi++;
	if(cxl_get_rng_buffi >= RNG_BUFSZ){
		cxl_get_rng_buffi = 0;
		cxl_buffered_rng(cxl_get_rng_buffer, RNG_BUFSZ, RNG_DIVISION);
	}

	return cxl_get_rng_buffer[cxl_get_rng_buffi];
}