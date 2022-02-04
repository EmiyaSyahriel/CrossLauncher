
//
// Created by EmiyaSyahriel on 20/05/2021.
//
#pragma once
#include <glm/vec2.hpp> // glm::vec3
#include <glm/vec3.hpp> // glm::vec3
#include <glm/vec4.hpp> // glm::vec4
#include <glm/mat4x4.hpp> // glm::mat4
#include <glm/ext/matrix_transform.hpp> // glm::translate, glm::rotate, glm::scale
#include <glm/ext/matrix_clip_space.hpp> // glm::perspective
#include <glm/ext/scalar_constants.hpp> // glm::pi

#if ANDROID
    #ifndef _PSW_GL_VER_
        #define _PSW_GL_VER_ "OpenGL ES 2.0"
    #endif
#elif WIN32
    #ifndef _PSW_GL_VER_
        #define _PSW_GL_VER_ "OpenGL 3.0"
   #endif _PSW_GL_VER_
#endif

typedef unsigned char byte;

#ifndef uint
#define uint unsigned int
#endif

enum class WAVE_TYPE : int8_t {
    PS3 = 0b0000,
    PSP = 0b0100,
    DEFAULT = 0b0000,
    PS3_NORMAL = 0b0000,
    PS3_BLINKS = 0b0010,
    PSP_BOTTOM = 0b0100,
    PSP_CENTER = 0b0110,
};

#ifndef POS_NORMALIZE
#define POS_NORMALIZE(t) (((float)t / (float)(xmb_detail_size - 1)) * 2) - 1
#endif

#ifndef HAS_FLAG
#define HAS_FLAG(source,target,s_type) ((static_cast<s_type>(source) & static_cast<s_type>(target)) == static_cast<s_type>(target))
#endif