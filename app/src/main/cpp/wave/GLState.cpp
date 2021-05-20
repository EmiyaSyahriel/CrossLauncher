
//
// Created by EmiyaSyahriel on 20/05/2021.
//

#include "GLRenderer.h"
#include "GLState.h"

int GLState::rw = 0;
int GLState::rh = 0;
float GLState::deltaTime = 0.0f;

GLRenderer *GLState::renderer = nullptr;
AAssetManager *GLState::assetmgr = nullptr;

void GLState::render(float deltaTime) {
    GLState::deltaTime = deltaTime;
    initiated(_render);
}

void GLState::create() {
    renderer = new GLRenderer();
}

void GLState::setup(int w, int h) {
    GLState::rw = w;
    GLState::rh = h;
    initiated(_setup);
}

void GLState::initiated(const std::function<void()> &func) {
    if(GLState::renderer != nullptr){
        func();
    }else{
        Log_e("Renderer is uninitialized!");
    }
}

void GLState::_setup() {
    Log_i("Setting up width %d x %d", GLState::rw, GLState::rh)
    renderer->setup(GLState::rw,GLState::rh);
}

void GLState::_render() {
    renderer->render(GLState::deltaTime);
}
