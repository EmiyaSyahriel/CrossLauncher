
cmake_minimum_required(VERSION 3.4.1)

include(EmbedRes.cmake)

set(GLM_VERSION "0.9.9")
project(glm VERSION ${GLM_VERSION} LANGUAGES CXX)
add_subdirectory(glm)
add_library(glm::glm ALIAS glm)

project(wave)

file(GLOB_RECURSE shaders "res/*.frag" "res/*.vert")
embed_res("R" "EMBEDRES" "wave" "shaders" "${shaders}")

file(GLOB_RECURSE wave_src "wave/*.cpp" "wave/*.h" "wave/*.hpp")

if (ANDROID) 
	set(ANDROID_WAVE_LIBS log EGL GLESv2 android glm)
	add_library(wave SHARED "Android.cpp" ${wave_src})
	target_link_libraries(wave ${ANDROID_WAVE_LIBS})
elseif(WIN32)
	find_package(GLFW3 CONFIG)
	find_package(GLEW CONFIG)
	add_library(wave STATIC ${wave_src})
	add_executable(wave_win32 "Win32.cpp")
	target_link_libraries(wave glm ${GLFW_LIBRARY} ${GLEW_LIBRARIES_STATIC})
	target_include_directories(wave PUBLIC ${GLFW_INCLUDE_DIR} ${GLEW_INCLUDE_DIRS})
	target_link_libraries(wave_win32 wave ${GLEW_LIBRARIES_STATIC})
endif()