
cmake_minimum_required(VERSION 3.4.1)

set(GLM_VERSION "0.9.9")
project(glm VERSION ${GLM_VERSION} LANGUAGES CXX)
add_subdirectory(glm)
add_library(glm::glm ALIAS glm)

project(wave)
file(GLOB WaveModuleSRC "wave/*.cpp" "wave/*.h")

if (ANDROID)
	add_library(wave SHARED XMBWaveModule.cpp ${WaveModuleSRC})
	target_link_libraries(wave log EGL GLESv2 android glm)
elseif(WIN32)

	include_directories("K:\\SDK\\ogl\\glfw-win64\\include")
	include_directories("K:\\SDK\\ogl\\glew\\include")
	link_libraries("K:\\SDK\\ogl\\glew\\lib\\Release\\x64\\glew32.lib")
	link_libraries("K:\\SDK\\ogl\\glfw-win64\\lib-static-ucrt\\glfw3dll.lib")
	
	add_library(wave STATIC ${WaveModuleSRC})
	add_executable(wave_win32 "WIN32.cpp")
	target_link_libraries(wave glm)
	target_link_libraries(wave_win32 wave)
endif()