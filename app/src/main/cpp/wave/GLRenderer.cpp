
//
// Created by EmiyaSyahriel on 20/05/2021.
//

#include "GLBind.h"

#if WIN32
#include "AAssetManager_win32.h"
#include <xstring>
#endif

#include <cstdio>
#include <cstdlib>
#include <cmath>
#include <vector>
#include <chrono>
#include "typedefs.h"
#include "Logger.h"
#include "GLRenderer.h"
#include "GLState.h"

bool GLRenderer::bIsLandscape = false;

GLRenderer::GLRenderer() {
    Log_i("Compiling shader...");
    compileShaders();
}

#define P_GLER checkForGlError(__LINE__);

void GLRenderer::render(float deltaTime) {
    // Log_i("Rendering at %fms", deltaTime*1000);
    // Do not render when paused
    // if(bIsPaused) return;

    currentTime += deltaTime;
    if(currentTime >= 65535) currentTime= 0;
    glClearColor(0.0,0.0,0.0,1.0);
    glClear( GL_DEPTH_BUFFER_BIT | GL_COLOR_BUFFER_BIT);

    drawBackground();
    drawWave();

    glFlush();
}

void GLRenderer::setup(int w, int h) {
    Log_i("GLState setup at %dx%d",w,h);
    width = w;
    height= h;
    GLRenderer::bIsLandscape = w > h;
    glViewport(0,0,w,h);
    clearVertexArray();

    glUseProgram(sBack);
    glUseProgram(sWave);
    if(glGetError() != GL_NO_ERROR){
        Log_i("Shader invalidated, re-compiling shader...");
        compileShaders();
    }

}

char* GLRenderer::readAsset(const char *filename) {
    AAsset* asset = AAssetManager_open(GLState::assetmgr, filename, AASSET_MODE_STREAMING);
    int size = AAsset_getLength(asset);
    char* buffer = new char[size];
    AAsset_read(asset, buffer, size);
    AAsset_close(asset);
    return buffer;
}

GLuint GLRenderer::compileShader(const char *vert, const char *frag, const char* name) {
    GLuint prog = glCreateProgram();
    GLuint vs = glCreateShader(GL_VERTEX_SHADER);
    GLuint fs = glCreateShader(GL_FRAGMENT_SHADER);


    glShaderSource(vs, 1, &vert, nullptr);
    glShaderSource(fs, 1, &frag, nullptr);
    glCompileShader(vs);
    glCompileShader(fs);
    compileCheckS(&vs, GL_COMPILE_STATUS, name);
    compileCheckS(&fs, GL_COMPILE_STATUS, name);
    glAttachShader(prog, vs);
    glAttachShader(prog, fs);
    glLinkProgram(prog);
    compileCheckP(&prog, GL_COMPILE_STATUS, name);
    return prog;
}

void GLRenderer::compileShaders() {
    char* backf = readAsset("xmb_background.frag");
    char* backv = readAsset("xmb_background.vert");
    char* wavef = readAsset("xmb_wave.frag");
    char* wavev = readAsset("xmb_wave.vert");
    sBack = compileShader(backv, backf, "back");
    sWave = compileShader(wavev, wavef, "wave");
    free(backf);
    free(backv);
    free(wavef);
    free(wavev);
}

void GLRenderer::compileCheckS(GLuint *any, GLenum type, const char *name) {
    GLint ibuffer=  GL_FALSE;
    glGetShaderiv(*any, type, &ibuffer);
    if(ibuffer != GL_FALSE){
        Log_i("Shader %s is compiled.", name);
    }else{
        GLint len = 0;
        glGetShaderiv(*any, GL_INFO_LOG_LENGTH, &len);
        if(len){
            char* buf = (char*) malloc(len);
            if(buf){
                glGetShaderInfoLog(*any, len, nullptr, buf);
                Log_e("Shader \"%s\" compilation failed : %s", name, buf);
                free(buf);
            }
        }
        any = nullptr;
    }
}

void GLRenderer::compileCheckP(GLuint *any, GLenum type, const char *name) {
    GLint ibuffer=  GL_FALSE;
    glGetProgramiv(*any, type, &ibuffer);
    if(ibuffer != GL_FALSE){
        Log_i("Program %s is compiled.", name);
    }else{
        GLint len = 0;
        glGetProgramiv(*any, GL_INFO_LOG_LENGTH, &len);
        if(len){
            char* buf = (char*) malloc(len);
            if(buf){
                glGetProgramInfoLog(*any, len, nullptr, buf);
                Log_e("Program \"%s\" compilation failed : %s", name, buf);
                free(buf);
            }
        }
        any = nullptr;
    }
}

void GLRenderer::drawBackground() {
    glUseProgram(sBack);
    GLint vpos = attrloc(sBack, "vpos");
    GLint uv = attrloc(sBack, "uv");
    GLint _cA = glGetUniformLocation(sBack, "_ColorA");
    GLint _cB = glGetUniformLocation(sBack, "_ColorB");

    float vertdata[3 * 6] = {
        -1,-1,0,
        -1, 1,0,
         1, 1,0,
        -1,-1,0,
         1,-1,0,
         1, 1,0
    };
    float uvdata[2*6] = {
            0,0,
            0,1,
            1,1,
            0,0,
            1,0,
            1,1
    };
    glVertexAttribPointer(vpos, 3, GL_FLOAT, GL_FALSE, 0, vertdata);
    glVertexAttribPointer(uv, 2, GL_FLOAT, GL_FALSE, 0, uvdata);
    glUniform3f(_cA, 0.00f, 0.66f, 1.00f);
    glUniform3f(_cB, 0.00f, 0.00f, 0.88f);
    glEnableVertexAttribArray(vpos);
    glEnableVertexAttribArray(uv);
    glDrawArrays(GL_TRIANGLES, 0, 6);
    glDisableVertexAttribArray(vpos);
    glDisableVertexAttribArray(uv);
}

void GLRenderer::drawWave() {
    glUseProgram(sWave);
    GLint pos = attrloc(sWave, "position");
    glEnable(GL_BLEND);
    glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
    GLint _cA = glGetUniformLocation(sWave, "white");
    GLint _cB = glGetUniformLocation(sWave, "color");
    GLint _cTime = glGetUniformLocation(sWave, "_Time");
    GLint _cScale = glGetUniformLocation(sWave, "_YScale");
    GLint _cStep = glGetUniformLocation(sWave, "_NormalStep");
    glUniform4f(_cA, 1.00f, 1.00f, 1.00f, 1.00f);
    glUniform4f(_cB, 1.00f, 1.00f, 1.00f, -0.25f);
    glUniform1f(_cScale, bIsLandscape ? 2.0f : 1.0f);
    glUniform1f(_cStep, bNormalSmoothing ? 2.0f / vertexGridSize :  0.001f);

    float _time = currentTime;
    float frac = 2.0f / vertexGridSize;

    glUniform1f(_cTime, _time);

    float x = -1.0f,z = -1.0f;
    while(x < 1.0f){
        z = -1.0f;
        while(z < 1.0f){
            float x0 = x;
            float z0 = z;
            float x1 = x + frac;
            float z1 = z + frac;

            glm::vec3 x0z0 = glm::vec3( x0, z0, 0);//calcWaveY(z0,x0,_time));
            glm::vec3 x1z0 = glm::vec3( x0, z1, 0);//calcWaveY(z1,x0,_time));
            glm::vec3 x0z1 = glm::vec3( x1, z0, 0);//calcWaveY(z0,x1,_time));
            glm::vec3 x1z1 = glm::vec3( x1, z1, 0);//calcWaveY(z1,x1,_time));

            auto
            *v1 = new Vertex(x0z0), *v2 = new Vertex(x1z1), *v3 = new Vertex(x0z1),
            // *v4 = v1, *v5 = new Vertex(x1z0), *v6 = v2; -> SIGSEGV
            *v4 = new Vertex(x0z0), *v5 = new Vertex(x1z0),*v6 = new Vertex(x1z1);

            vbuffer.push_back(v1);
            vbuffer.push_back(v2);
            vbuffer.push_back(v3);
            vbuffer.push_back(v4);
            vbuffer.push_back(v5);
            vbuffer.push_back(v6);

            z+= frac;
        }
        x +=frac;
    }

    int nCalcSize = 3;

    /// UNPACK DATA - we need two arrays, as one combined will cause a complex effect

#if ANDROID
    // vertex position data
    float vposdata[vbuffer.size() * 3];
    // vertex normal data
    float normdata[vbuffer.size() * 3];
#elif WIN32
    float* vposdata = new float[vbuffer.size() * 3];
    float* normdata = new float[vbuffer.size() * 3];
#endif
    for(int i =0; i < vbuffer.size(); i++){

        float sScale = bIsLandscape ? 2.0F : 1.0F;

        int ii = i * 3;
        vposdata[ii + 0] = vbuffer[i]->position.x * 1.1F;
        vposdata[ii + 1] = vbuffer[i]->position.y * sScale;
        vposdata[ii + 2] = vbuffer[i]->position.z;
    }

    // draw
    glVertexAttribPointer(pos, 3, GL_FLOAT, GL_FALSE, 0, vposdata);
    checkForGlError(261);
    glEnableVertexAttribArray(pos);
    glLineWidth(4.0f);
    glDrawArrays(GL_TRIANGLES, 0, vbuffer.size());
    checkForGlError(265);
    clearVertexArray();
}

void GLRenderer::setVec1(GLuint pid, const char *attrName, GLfloat v) {glVertexAttrib1f(attrloc(pid, attrName), v); }
void GLRenderer::setVec2(GLuint pid, const char *attrName, GLfloat v1, GLfloat v2) { glVertexAttrib2f(attrloc(pid, attrName), v1, v2); }
void GLRenderer::setVec3(GLuint pid, const char *attrName, GLfloat v1, GLfloat v2, GLfloat v3) { glVertexAttrib3f(attrloc(pid, attrName), v1,v2,v3); }
void GLRenderer::setVec4(GLuint pid, const char *attrName, GLfloat v1, GLfloat v2, GLfloat v3, GLfloat v4) { glVertexAttrib4f(attrloc(pid, attrName), v1,v2,v3,v4); }
GLint GLRenderer::attrloc(GLuint pid, const char *attrName) { return glGetAttribLocation(pid, attrName); }

float GLRenderer::calcWaveY(float x, float z, float t) {
    // x = x * (bIsLandscape ? 2.0f : 1.0f);
    float bigwave = sin(x + (z * 0.5F) + (t * 0.25F));
    float med = sin((x * 2) + z + (t * 1.0F));
    float xmed = sin((x * 1.5F) + (z * 4) + (t * 1.2F));

    med = rRange(med);
    xmed = rRange(xmed);

    xmed = std::fmax(xmed, 0);

    float smallwave = (med * xmed) * 0.05F;
    float retval = (bigwave + smallwave) / 2.0F;
    retval = rRange(retval);
    return (retval * 0.3F) + 0.2f;
}

float GLRenderer::rRange(float x) {
    return (x * 2.0f) - 1.0f;
}

void GLRenderer::clearVertexArray() {
    for(auto v : vbuffer){
        delete v;
    }
    vbuffer.clear();
}

uint GLRenderer::clamp(uint val, uint min, uint max) {
    return fmax(min, fmin(max, val));
}

uint GLRenderer::fsmod(unsigned int val, unsigned int min, unsigned int max) {
    auto total = max - min;
    auto delta = fmod(val - min, total);
    return min + (unsigned int)delta;
}

void GLRenderer::checkForGlError(int line) {
    auto err = glGetError();
    if(err != GL_NO_ERROR){
        Log_i("GL ERROR (%d): %s", line, gelGetErrorString(err))
    }
}

char const *GLRenderer::gelGetErrorString(GLenum err) {
        switch (err)
        {
            // opengl 2 errors (8)
            case GL_NO_ERROR:
                return "GL_NO_ERROR";

            case GL_INVALID_ENUM:
                return "GL_INVALID_ENUM";

            case GL_INVALID_VALUE:
                return "GL_INVALID_VALUE";

            case GL_INVALID_OPERATION:
                return "GL_INVALID_OPERATION";

            case GL_OUT_OF_MEMORY:
                return "GL_OUT_OF_MEMORY";

                // opengl 3 errors (1)
            case GL_INVALID_FRAMEBUFFER_OPERATION:
                return "GL_INVALID_FRAMEBUFFER_OPERATION";

                // gles 2, 3 and gl 4 error are handled by the switch above
            default:
                assert(!"unknown error");
                return nullptr;
        }

}

Vertex* GLRenderer::getVertexAt(uint x, uint y) {
    uint i = (fsmod(y, 0, vertexGridSize - 1) + ((fsmod(x, 0, vertexGridSize - 1) * vertexGridSize)));
    return vbuffer[i];
};

const int
glr_Ax = 0, glr_Ay = 1, glr_Az = 2,
glr_Bx = 3, glr_By = 4, glr_Bz = 5,
glr_Cx = 6, glr_Cy = 7, glr_Cz = 8;
