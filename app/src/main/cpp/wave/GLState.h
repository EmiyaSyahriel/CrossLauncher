
//
// Created by EmiyaSyahriel on 20/05/2021.
//
#pragma once

#include <functional>
#include <android/asset_manager.h>
#include "Logger.h"
#include "GLRenderer.h"

class GLState{
private:
    static GLRenderer *renderer;
    static void initiated(const std::function<void()> &func);
    static void _setup();
    static void _render();

    static int rw;
    static int rh;
    static float deltaTime;
public:
    static AAssetManager *assetmgr;
    static void create();
    static void setup(int w, int h);
    static void render(float deltaTime);
};

