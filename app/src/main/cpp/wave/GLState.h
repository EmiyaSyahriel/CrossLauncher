
//
// Created by EmiyaSyahriel on 20/05/2021.
//
#pragma once

#include <functional>
#include "Logger.h"
#include <cstdlib>
#include "GLRenderer.h"
#if ANDROID
#include <android/asset_manager.h>
#elif WIN32
#include "AAssetManager_win32.h"
#endif

class GLState{
private:
    static dictionary<uint32_t, GLRenderer> renderers;

    static GLRenderer *renderer;
    static void initiated(const std::function<void()> &func);
    static void _setup();
    static void _render();
    static void _pause();

    static int rw;
    static int rh;
    static float deltaTime;
    static bool bIsPaused;
public:
    static AAssetManager *assetmgr;
    static void create();
    static void setup(int w, int h);
    static void render(float deltaTime);
    static void pause(bool bPaused);
    static bool bGetIsPaused();

    static void kill();
};

