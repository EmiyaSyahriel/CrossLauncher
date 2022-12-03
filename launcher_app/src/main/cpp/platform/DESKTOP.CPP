#include <cstdlib>
#include <cstdio>
#ifdef __GNUC__
#include <strings.h>
#else
#include <string.h>
#endif
#include "GL/glew.h"
#include "../wave/WAVE.HPP"
#include <GLFW/glfw3.h>

bool w32_hasInit = false;

/* Unix doesn't have strcmpi, substitute it with strcasecmp  */
#ifdef __GNUC__
inline int strcmpi(const char* a, const char* b){
	return strcasecmp(a, b);
}
#endif

const char* getarg(int argc, const char** argv, const char* arg) {
	if (arg == nullptr) return nullptr;
	for (int i = 0; i < argc; i++) {
		const char* argn = argv[i];
		if (strcmpi(argn, arg) == 0) {
			if (i + 1 < argc) return argv[i + 1];
		}
	}
	return nullptr;
}
bool hasarg(int argc, const char** argv, const char* arg) {
	if (arg == nullptr) return false;
	for (int i = 0; i < argc; i++) {
		const char* argn = argv[i];
		if (strcmpi(argn, arg) == 0) {
			return true;
		}
	}
	return false;
}

const char* getarg_alias(int argc, const char** argv, int rargc, const char** rargv) {
	for (int i = 0; i < rargc; i++) {
		const char* value = getarg(argc, argv, rargv[i]);
		if (value != nullptr) return value;
	}
	return nullptr;
}
bool hasarg_alias(int argc, const char** argv, int rargc, const char** rargv) {
	for (int i = 0; i < rargc; i++) {
		if (hasarg(argc, argv, rargv[i])) return true;
	}
	return false;
}

static void w32_init() {
	cxl_wave_start();
	glEnable(GL_MULTISAMPLE);
}

float w32_cTime = 0.0f;

void w32_render(GLFWwindow* window) {
	float s = (float)glfwGetTime();

	glfwMakeContextCurrent(window);

	if (!w32_hasInit) {
		glewInit();
		w32_init();
		w32_hasInit = true;
	}
	cxl_wave_draw(s - w32_cTime);
	w32_cTime = s;
	glfwSwapBuffers(window);
}

static void w32_resize(GLFWwindow* window, int width, int height) {
	cxl_wave_resize((float)width, (float)height);
	w32_render(window);
}

void pause() {
	char pause = getchar();
}

template <typename T>
static void parse_data(const char* str, const char* format, T* data, const char* defval) {
	str = str == nullptr ? defval : str;
	if (str != nullptr) {
		if (sscanf(str, format, data) == 0){
			Log_d("Unable to parse argument");
		}
	}
}

static const char* speed_arg[] = { "-s","--speed" };
static const char* style_arg[] = { "-x","--style" };
static const char* day_night_arg[] = { "-dn","--daynight" };
static const char* monthly_arg[] = { "-mn","--monthly" };
static const char* backa_arg[] = { "-ba","--backcolor_a" };
static const char* backb_arg[] = { "-bb","--backcolor_b" };
static const char* forea_arg[] = { "-fa","--forecolor_a" };
static const char* foreb_arg[] = { "-fb","--forecolor_b" };

static void w32_readargs(int argc, const char** argv) {
	const char* speed = getarg_alias(argc, argv, 2, speed_arg);
	const char* style = getarg_alias(argc, argv, 2, style_arg);
	const char* backcolor_a = getarg_alias(argc, argv, 2, backa_arg);
	const char* backcolor_b = getarg_alias(argc, argv, 2, backb_arg);
	const char* forecolor_a = getarg_alias(argc, argv, 2, forea_arg);
	const char* forecolor_b = getarg_alias(argc, argv, 2, foreb_arg);

	bool day_night = hasarg_alias(argc, argv, 2, day_night_arg);
	bool monthly =   hasarg_alias(argc, argv, 2, monthly_arg);

	int backa, backb, forea, foreb;

	// put all the data
	parse_data(speed, "%f", &wave_state.speed, "1.0");

	parse_data(backcolor_a, "%x", &backa, "FF9900FF");
	parse_data(backcolor_b, "%x", &backb, "FF0099FF");
	parse_data(forecolor_a, "%x", &forea, "FFFFFFFF");
	parse_data(forecolor_b, "%x", &foreb, "88FFFFFF");

	if (style == nullptr) style = "DEFAULT";

	if (strcmpi(style, "DEFAULT") == 0)    wave_state.style = WAVE_STYLE::DEFAULT;
	if (strcmpi(style, "PS3_NORMAL") == 0) wave_state.style = WAVE_STYLE::PS3_NORMAL;
	if (strcmpi(style, "PS3_BLINKS") == 0) wave_state.style = WAVE_STYLE::PS3_BLINKS;
	if (strcmpi(style, "PSP_CENTER") == 0) wave_state.style = WAVE_STYLE::PSP_CENTER;
	if (strcmpi(style, "PSP_BOTTOM") == 0) wave_state.style = WAVE_STYLE::PSP_BOTTOM;

	wave_state.colors.bg_a = cxl_i2v4(backa);
	wave_state.colors.bg_b = cxl_i2v4(backb);
	wave_state.colors.fg_a = cxl_i2v4(forea);
	wave_state.colors.fg_b = cxl_i2v4(foreb);
	
	wave_state.bg_month = MONTH_COLOR_INDEX::MONTH_CUSTOM;
	if(day_night) wave_state.day_night = true;
	if(monthly) wave_state.bg_month = MONTH_COLOR_INDEX::MONTH_CURRENT;
}

void w32_key(GLFWwindow* win, int key, int scn, int act, int mod) {
	if (act == GLFW_PRESS) {
		if (key == GLFW_KEY_BACKSLASH) {
			switch (wave_state.style) {
			case WAVE_STYLE::PS3_BLINKS: wave_state.style = WAVE_STYLE::PS3_NORMAL; break;
			case WAVE_STYLE::PS3_NORMAL: wave_state.style = WAVE_STYLE::PSP_CENTER; break;
			case WAVE_STYLE::PSP_CENTER: wave_state.style = WAVE_STYLE::PSP_BOTTOM; break;
			case WAVE_STYLE::PSP_BOTTOM: wave_state.style = WAVE_STYLE::PS3_BLINKS; break;
			}
		}
		if (key == GLFW_KEY_PAGE_UP) {
			wave_state.speed += 0.15f;
			printf("Wave Speed : %f\n", wave_state.speed);
		}
		if (key == GLFW_KEY_PAGE_DOWN) {
			wave_state.speed -= 0.15f;
			printf("Wave Speed : %f\n", wave_state.speed);
		}
	}
}

int main(int argc, const char** argv) {
	if (!glfwInit()) {
		printf("Unable to initialize GLFW\n"); 
		pause();
		return -1; 
	}
	w32_readargs(argc, argv);
	glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
	glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 0);
	glfwWindowHint(GLFW_SAMPLES, 4);

	GLFWwindow* window = glfwCreateWindow(1280, 720, "libwave", NULL, NULL);
	if (!window) {
		glfwTerminate();
		printf("Cannot create a window, terminating...\n");
		pause();
		return -1;
	}
	cxl_wave_resize((float)1280, (float)720);
	glfwSetWindowSizeCallback(window, w32_resize);
	glfwSetKeyCallback(window, w32_key);

	while (!glfwWindowShouldClose(window)) {
		w32_render(window);
		glfwPollEvents();
	}
	
	glfwTerminate();
	return 0;
}
