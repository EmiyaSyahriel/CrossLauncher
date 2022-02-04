//
// Created by ainaa on 17/11/2021.
//

#include "mathutil.h"
#include <chrono>

float timeofday() {
	time_t now = time(0);
	tm* today = localtime(&now);
	return (((float)today->tm_hour * 60) + today->tm_min);
}

//   __   || 
// _/  \_ || 
float timeofday_shader(float clock) {
	float w = clock / 1440.0f;
	float retval = 0.0f;
	if (w >= 0.16f && w < 0.33f) retval = 1.0f - unrange(cos(arclerp(0.16f, 0.33f, w)));
	else if (w >= 0.33f && w < 0.66f) retval = 1.0f;
	else if (w >= 0.66f && w < 0.82f) retval = unrange(cos(arclerp(0.66f, 0.82f, w)));
	else retval = 0.0f;
	return retval;
}

int monthofday() {
	time_t now = time(0);
	tm* today = localtime(&now);
	return today->tm_mon;
}

glm::vec2 get_month_uv(int month)
{
	float x = (fmod((float)month, 4.0f) + (0.5f)) / 4.0f;
	float y = (((float)month / 4.0f) + (0.5f)) / 3.0f;
	return glm::vec2(x, y);
}
