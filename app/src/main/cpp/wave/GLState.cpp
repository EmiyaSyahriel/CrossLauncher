
//
// Created by EmiyaSyahriel on 20/05/2021.
//

#include "GLRenderer.h"
#include "GLState.h"
#include "Logger.h"

int GLState::rw = 0;
int GLState::rh = 0;
float GLState::deltaTime = 0.0f;
bool GLState::bIsPaused = false;

GLRenderer *GLState::renderer = nullptr;
AAssetManager *GLState::assetmgr = nullptr;

void GLState::render(float deltaTime) {
    GLState::deltaTime = deltaTime;
    initiated(_render);
}

void GLState::create() {
    if(renderer != nullptr) return;
    renderer = new GLRenderer();
}

void GLState::setup(int w, int h) {
    GLState::rw = w;
    GLState::rh = h;
    initiated(_setup);
}

void GLState::initiated(const std::function<void()> &func) {
    if(GLState::renderer == nullptr){
        Log_e("Renderer is uninitialized or has been deleted! Creating...");
        GLState::create();
    }
    func();
}

void GLState::_setup() {
    Log_i("Setting up width %d x %d", GLState::rw, GLState::rh)
    renderer->setup(GLState::rw,GLState::rh);
}

void GLState::_render() {
    renderer->render(GLState::deltaTime);
}

void GLState::pause(bool bPaused) {
    GLState::bIsPaused = bPaused;
    initiated(_pause);
}

void GLState::_pause() {
    renderer->bIsPaused = GLState::bIsPaused;
}

bool GLState::bGetIsPaused() {
    if(renderer){
        return renderer->bIsPaused;
    }
    return true;
}

void GLState::kill() {
    delete renderer;
}
