#define GLEW_STATIC
#include <Windows.h>
#include <cstdlib>
#include <cstdio>
#include <string>
#include "GL/glew.h"
#include "wave/wave.h"
#include "wave/Logger.h"
#include <glfw/glfw3.h>
#include "wave/states.h"
#include "wave/mathutil.h"

bool w32_hasInit = false;

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

const char* getarg_alias(int argc, const char** argv, int rargc, const char** rargv) {
	for (int i = 0; i < rargc; i++) {
		const char* value = getarg(argc, argv, rargv[i]);
		if (value != nullptr) return value;
	}
	return nullptr;
}

static void w32_init() {
	wave_start();
	glEnable(GL_MULTISAMPLE);
}

void w32_render(GLFWwindow* window) {

	glfwMakeContextCurrent(window);

	if (!w32_hasInit) {
		glewInit();
		w32_init();
		w32_hasInit = true;
	}
	wave_draw(0.16f);
	glfwSwapBuffers(window);
}

static void w32_resize(GLFWwindow* window, int width, int height) {
	wave_resize((float)width, (float)height);
	w32_render(window);
}


void pause() {
	char pause = getchar();
}

template <typename T>
static void parse_data(const char* str, const char* format, T* data, const char* defval) {
	str = str == nullptr ? defval : str;
	if (str != nullptr) {
		if (sscanf(str, format, data)){
			Log_d("Unable to parse argument");
		}
	}
}

static const char* speed_arg[] = { "-s","--speed" };
static const char* style_arg[] = { "-x","--style" };
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

	int backa, backb, forea, foreb;

	// put all the data
	parse_data(speed, "%f", &xmb_wave_speed, "1.0");

	

	parse_data(backcolor_a, "%x", &backa, "FF9900FF");
	parse_data(backcolor_b, "%x", &backb, "FF0099FF");
	parse_data(forecolor_a, "%x", &forea, "FFFFFFFF");
	parse_data(forecolor_b, "%x", &foreb, "88FFFFFF");

	if (style == nullptr) style = "DEFAULT";

	if (strcmpi(style, "DEFAULT") == 0) wave_type = WAVE_TYPE::DEFAULT;
	if (strcmpi(style, "PS3_NORMAL") == 0) wave_type = WAVE_TYPE::PS3_NORMAL;
	if (strcmpi(style, "PS3_BLINKS") == 0) wave_type = WAVE_TYPE::PS3_BLINKS;
	if (strcmpi(style, "PSP_CENTER") == 0) wave_type = WAVE_TYPE::PSP_CENTER;
	if (strcmpi(style, "PSP_BOTTOM") == 0) wave_type = WAVE_TYPE::PSP_BOTTOM;

	background_color_top = int2color(backa);
	background_color_bottom = int2color(backb);
	foreground_color_edge = int2color(forea);
	foreground_color_center = int2color(foreb);
}

int main(int argc, const char** argv) {
	if (!glfwInit()) {
		printf("Unable to initialize GLFW"); 
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
		printf("Cannot create a window, terminating...");
		pause();
		return -1;
	}

	glfwSetWindowSizeCallback(window, w32_resize);

	while (!glfwWindowShouldClose(window)) {
		w32_render(window);
		glfwPollEvents();
	}
	
	glfwTerminate();
	return 0;
}
