
//
// Created by EmiyaSyahriel on 20/05/2021.
//

#pragma once

#include <GLES2/gl2.h>
#include <GLES2/gl2ext.h>
#include "typedefs.h"

class GLRenderer{
private:
    int width = 0;
    int height = 0;

    GLuint sBlank = 0;
    GLuint sBack = 0;
    GLuint sWave = 0;

    GLuint aWave = 0;
    GLuint aBack = 0;
    GLuint bWave = 0;
    GLuint bBack = 0;

    float currentTime = 0.0f;

    static GLint attrloc(GLuint pid, const char* attrName);
    static void setVec1(GLuint pid, const char* attrName, GLfloat v);
    static void setVec2(GLuint pid, const char* attrName, GLfloat v1, GLfloat v2);
    static void setVec3(GLuint pid, const char* attrName, GLfloat v1, GLfloat v2, GLfloat v3);
    static void setVec4(GLuint pid, const char *attrName, GLfloat v1, GLfloat v2, GLfloat v3, GLfloat v4);

public:
    GLRenderer();
    void setup(int w, int h);
    void render(float deltaTime);
    void compileShaders();

    void drawWave();
    void drawBackground();

    static byte* readAsset(const char *filename);

    static GLuint compileShader(const char* vert, const char* frag, const char* name);
    static void compileCheckS(GLuint *any, GLenum type, const char* name);
    static void compileCheckP(GLuint *any, GLenum type, const char* name);
};

