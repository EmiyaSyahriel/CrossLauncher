
cmake_minimum_required(VERSION 3.4.1)

set(GLM_VERSION "0.9.9")
project(glm VERSION ${GLM_VERSION} LANGUAGES CXX)
add_subdirectory(glm)
add_library(glm::glm ALIAS glm)

file(GLOB WaveModuleSRC "wave/*.cpp" "wave/*.h")
add_library(wave SHARED XMBWaveModule.cpp ${WaveModuleSRC})

target_link_libraries(wave log EGL GLESv2 android glm)