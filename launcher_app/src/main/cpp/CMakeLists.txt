
cmake_minimum_required(VERSION 3.4.1)

include(EmbedRes.cmake)

set(GLM_VERSION "0.9.9")
project(glm VERSION ${GLM_VERSION} LANGUAGES CXX)
add_subdirectory(glm)
add_library(glm::glm ALIAS glm)

project(wave)

file(GLOB_RECURSE shaders "res/*.frag" "res/*.vert")
embed_res("R" "EMBEDRES" "wave" "SHADERS" "${shaders}")

file(GLOB_RECURSE wave_src "wave/*.CPP" "wave/*.HPP")

if (ANDROID) 
	set(ANDROID_WAVE_LIBS log EGL GLESv2 android glm)
	add_library(wave SHARED "platform/ANDROID.CPP" ${wave_src})
	target_link_libraries(wave ${ANDROID_WAVE_LIBS})
else()
	find_package(glfw3 CONFIG REQUIRED)
	find_package(GLEW REQUIRED)
	find_package(OpenGL REQUIRED)
	message("Is Glew found? : ${GLEW_FOUND} -> ${GLEW_VERSION}")
	add_executable(wave "platform/DESKTOP.CPP" ${wave_src})
	# target_include_directories(wave PUBLIC ${GLEW_INCLUDE_DIRS} ${GLFW_INCLUDE_DIR})
	target_link_libraries(wave PRIVATE glm::glm glfw GLEW::GLEW OpenGL32)
endif()

if(ANDROID)
	add_library(vulkanisir SHARED "vkcheck/MAIN.CPP")
	target_link_libraries(vulkanisir PRIVATE dl)
endif()