
//
// Created by EmiyaSyahriel on 20/05/2021.
//

#pragma once
#include "GLBind.h"
#include "typedefs.h"



class GLRenderer{
private:
    int width = 0;
    int height = 0;
    static bool bIsLandscape;

    GLuint sBack = 0;
    GLuint sWave = 0;

    std::vector<Vertex*> vbuffer = std::vector<Vertex*>();
    float currentTime = 0.0f;

    static GLint attrloc(GLuint pid, const char* attrName);
    static void setVec1(GLuint pid, const char* attrName, GLfloat v);
    static void setVec2(GLuint pid, const char* attrName, GLfloat v1, GLfloat v2);
    static void setVec3(GLuint pid, const char* attrName, GLfloat v1, GLfloat v2, GLfloat v3);
    static void setVec4(GLuint pid, const char *attrName, GLfloat v1, GLfloat v2, GLfloat v3, GLfloat v4);
    static uint clamp(uint val, uint min, uint max);
    static uint fsmod(uint val, uint min, uint max);
    static float calcWaveY(float x, float z, float t);
    static float rRange(float x);

    static void checkForGlError(int line);
    static char const* gelGetErrorString(GLenum const err);


    Vertex* getVertexAt(uint x, uint y);
public:
    bool bIsPaused = false;

    GLRenderer();

    void setup(int w, int h);
    void render(float deltaTime);
    void compileShaders();

    void drawWave();
    void drawBackground();
    void clearVertexArray();

    static char* readAsset(const char *filename);

    static GLuint compileShader(const char* vert, const char* frag, const char* name);
    static void compileCheckS(GLuint *any, GLenum type, const char* name);
    static void compileCheckP(GLuint *any, GLenum type, const char* name);
    uint vertexGridSize = 16;
    bool bNormalSmoothing = true;
};

