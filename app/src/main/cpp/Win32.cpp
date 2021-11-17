#define GLEW_STATIC
#include <Windows.h>
#include <cstdlib>
#include <cstdio>
#include "GL/glew.h"
#include "wave/wave.h"
#include "wave/Logger.h"
#include <glfw/glfw3.h>
#include "wave/states.h"

bool w32_hasInit = false;

static void w32_init() {
	xmb_wave_speed = 0.25f;
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

int main() {


	if (!glfwInit()) {
		printf("Unable to initialize GLFW"); 
		pause();
		return -1; 
	}

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
