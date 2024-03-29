cmake_minimum_required(VERSION 3.4.1)
set(GLM_VERSION "0.9.9")
project(glm VERSION ${GLM_VERSION} LANGUAGES CXX)
add_subdirectory(glm)
add_library(glm::glm ALIAS glm)

project(wave)

file(GLOB_RECURSE wave_src "wave/*.CPP")
file(GLOB_RECURSE canyon_src "canyon/*.CPP")
file(GLOB_RECURSE gaia_src "gaia/*.CPP")

if (ANDROID) 
	set(ANDROID_GL_LIBS log EGL GLESv2 android glm)
	add_library(wave SHARED "platform/android/WAVE.CPP" ${wave_src})
	add_library(gaia SHARED "platform/android/GAIA.CPP" ${gaia_src})
	add_library(canyon SHARED "platform/android/CANYON.CPP" ${canyon_src})
	target_link_libraries(wave ${ANDROID_GL_LIBS})
	target_link_libraries(gaia ${ANDROID_GL_LIBS})
	target_link_libraries(canyon ${ANDROID_GL_LIBS})
else()
	find_package(glfw3 CONFIG REQUIRED)
	find_package(GLEW REQUIRED)
	find_package(OpenGL REQUIRED)
	message("Is Glew found? : ${GLEW_FOUND} -> ${GLEW_VERSION}")
	add_executable(wave "platform/desktop/WAVE.CPP" ${wave_src})
	add_executable(gaia "platform/desktop/GAIA.CPP" ${gaia_src})
	add_executable(canyon "platform/desktop/CANYON.CPP" ${canyon_src})
	set(DESKTOP_LIBS glm::glm glfw GLEW::GLEW OpenGL32)
	target_link_libraries(wave PRIVATE ${DESKTOP_LIBS})
	target_link_libraries(gaia PRIVATE ${DESKTOP_LIBS})
	target_link_libraries(canyon PRIVATE ${DESKTOP_LIBS})
endif()

if(ANDROID)
	add_library(vulkanisir SHARED "vkcheck/MAIN.CPP")
	target_link_libraries(vulkanisir PRIVATE dl)
endif()