
#pragma once

#if ANDROID
#include <GLES2/gl2.h>
#include <GLES2/gl2ext.h>
#include <android/asset_manager.h>
#include <android/asset_manager_jni.h>
#elif WIN32

#ifdef _MSC_VER                         // Check if MS Visual C compiler
#  pragma comment(lib, "opengl32.lib")  // Compiler-specific directive to avoid manually configuration
#  pragma comment(lib, "glu32.lib")     // Link libraries
#  pragma comment(lib, "glew32.lib")
#endif
#define WIN32_LEAN_AND_MEAN
#include <windows.h>
#include <gl/glew.h>
#endif