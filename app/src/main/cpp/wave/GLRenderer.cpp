
//
// Created by EmiyaSyahriel on 20/05/2021.
//
#include <GLES2/gl2.h>
#include <GLES2/gl2ext.h>
#include <android/asset_manager.h>
#include <android/asset_manager_jni.h>
#include <cstdio>
#include <cstdlib>
#include <cmath>
#include <vector>
#include <chrono>
#include "Logger.h"
#include "GLRenderer.h"
#include "GLState.h"

GLRenderer::GLRenderer() {
    Log_i("Compiling shader...");
    compileShaders();

}

void GLRenderer::render(float deltaTime) {
    currentTime += deltaTime;
    if(currentTime >= 65535) currentTime= 0;
    glClearColor(0.0,0.66,1.0,1.0);
    glClear(GL_DEPTH_BUFFER_BIT | GL_COLOR_BUFFER_BIT);
    glLineWidth(1.0f);
    drawBackground();
    drawWave();
}

void GLRenderer::setup(int w, int h) {
    width = w;
    height= h;
    glViewport(0,0,w,h);
}

byte* GLRenderer::readAsset(const char *filename) {
    AAsset *asset = AAssetManager_open(GLState::assetmgr, filename,AASSET_MODE_STREAMING);
    int size = AAsset_getLength(asset);
    byte* buffer = (byte*)malloc(size);
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
    byte* blankf = readAsset("blank.frag");
    byte* blankv = readAsset("blank.vert");
    byte* backf = readAsset("xmb_background.frag");
    byte* backv = readAsset("xmb_background.vert");
    byte* wavef = readAsset("xmb_wave.frag");
    byte* wavev = readAsset("xmb_wave.vert");
    sBlank = compileShader((char*)blankv, (char*)blankf, "blank");
    sBack = compileShader((char*)backv, (char*)backf, "back");
    sWave = compileShader((char*)wavev, (char*)wavef, "wave");
    free(blankf);
    free(blankv);
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
                Log_e("Shader \"%s\" compilation failed : %s", name, buf);
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
    glUniform3f(_cA, 0.00f, 0.00f, 0.8f);
    glUniform3f(_cB, 0.00f, 0.22f, 1.0f);
    glEnableVertexAttribArray(vpos);
    glEnableVertexAttribArray(uv);
    glDrawArrays(GL_TRIANGLES, 0, 6);
    glDisableVertexAttribArray(vpos);
    glDisableVertexAttribArray(uv);
    glFlush();
}

void GLRenderer::drawWave() {
    glUseProgram(sWave);
    GLint pos = attrloc(sWave, "position");
    GLint normal = attrloc(sWave, "normal");

    glEnable(GL_BLEND);
    glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
    GLint _cA = glGetUniformLocation(sWave, "white");
    GLint _cB = glGetUniformLocation(sWave, "color");
    glUniform4f(_cA, 1.00f, 1.00f, 1.0f, 1.0f);
    glUniform4f(_cB, 1.00f, 1.00f, 1.0f, -0.1f);

    // generate vertex
    // format : px py pz ux uy nx ny nz
    std::vector<Vertex*> vbuffer;
    float _time = currentTime;
    float frac = 2.0f/16.0f;
    float x = -1.0f,z = -1.0f;
    while(z < 1.0f){
        x = -1.0f;
        while(x < 1.0f){
            float x0 = x;
            float z0 = z;
            float x1 = x + frac;
            float z1 = z + frac;

            // TODO: Remake the wave-y calculation
            glm::vec3 x0z0 = glm::vec3(x0,sin(tan(x0) + z0 + _time), z0);
            glm::vec3 x1z0 = glm::vec3(x1,sin(tan(x1) + z0 + _time), z0);
            glm::vec3 x0z1 = glm::vec3(x0,sin(tan(x0) + z1 + _time), z1);
            glm::vec3 x1z1 = glm::vec3(x1,sin(tan(x1) + z1 + _time), z1);

            auto
            *v1 = new Vertex(x0z0), *v2 = new Vertex(x1z1), *v3 = new Vertex(x0z1),
            *v4 = new Vertex(x0z0), *v5 = new Vertex(x1z0),*v6 = new Vertex(x1z1);

            vbuffer.push_back(v1);
            vbuffer.push_back(v2);
            vbuffer.push_back(v3);
            vbuffer.push_back(v4);
            vbuffer.push_back(v5);
            vbuffer.push_back(v6);

            x+= frac;

        }
        z +=frac;
    }

    // calculate normal
    for(int i = 0; i < vbuffer.size(); i+=3){
        glm::vec3 nrm = glm::vec3(0,0,0);
        for(int ii = 0; ii < 3; ii++){
            glm::vec3 c = vbuffer[i + ii]->position;
            glm::vec3 n = vbuffer[i + fmod(ii + 1, 3)]->position;
            nrm.x += (c.y - n.y) * (c.z - n.z);
            nrm.y += (c.z - n.z) * (c.x - n.x);
            nrm.z += (c.x - n.x) * (c.y - n.y);
        }
        glm::vec3 nnrm =glm::normalize(nrm);
        vbuffer[i + 0]->normal = nnrm;
        vbuffer[i + 1]->normal = nnrm;
        vbuffer[i + 2]->normal = nnrm;
    }

    // smooth out normals
    for(int i =0 ; i < vbuffer.size(); i++){
        glm::vec3 nrm = vbuffer[i]->normal;
        nrm += vbuffer[fmod(i+3, vbuffer.size())]->normal;
        vbuffer[i]->normal = glm::normalize(nrm);
    }

    /// UNPACK DATA - we need two arrays, as one combined will cause a complex effect

    // vertex position data
    float vposdata[vbuffer.size() * 3];
    // vertex normal data
    float normdata[vbuffer.size() * 3];
    for(int i =0; i < vbuffer.size(); i++){
        int ii = i * 3;
        vposdata[ii + 0] = vbuffer[i]->position.x;
        vposdata[ii + 1] = vbuffer[i]->position.y;
        vposdata[ii + 2] = vbuffer[i]->position.z;
        normdata[ii + 0] = vbuffer[i]->normal.x;
        normdata[ii + 1] = vbuffer[i]->normal.y;
        normdata[ii + 2] = vbuffer[i]->normal.z;

        float y = vposdata[ii + 1];
        y = (y * 2.0f) - 1.0f;
        vposdata[ii + 1] = y * 0.1f;
        delete vbuffer[i];
    }

    // draw
    glVertexAttribPointer(pos, 3, GL_FLOAT, GL_FALSE, 0, vposdata);
    glVertexAttribPointer(normal, 3, GL_FLOAT, GL_FALSE, 0, normdata);
    glEnableVertexAttribArray(pos);
    glEnableVertexAttribArray(normal);
    glDrawArrays(GL_TRIANGLES, 0, vbuffer.size());
    glDisableVertexAttribArray(pos);
    glDisableVertexAttribArray(normal);
    glDisable(GL_BLEND);
    glFlush();
    vbuffer.clear();
}

void GLRenderer::setVec1(GLuint pid, const char *attrName, GLfloat v) {glVertexAttrib1f(attrloc(pid, attrName), v); }
void GLRenderer::setVec2(GLuint pid, const char *attrName, GLfloat v1, GLfloat v2) { glVertexAttrib2f(attrloc(pid, attrName), v1, v2); }
void GLRenderer::setVec3(GLuint pid, const char *attrName, GLfloat v1, GLfloat v2, GLfloat v3) { glVertexAttrib3f(attrloc(pid, attrName), v1,v2,v3); }
void GLRenderer::setVec4(GLuint pid, const char *attrName, GLfloat v1, GLfloat v2, GLfloat v3, GLfloat v4) { glVertexAttrib4f(attrloc(pid, attrName), v1,v2,v3,v4); }
GLint GLRenderer::attrloc(GLuint pid, const char *attrName) { return glGetAttribLocation(pid, attrName); }

const int
glr_Ax = 0, glr_Ay = 1, glr_Az = 2,
glr_Bx = 3, glr_By = 4, glr_Bz = 5,
glr_Cx = 6, glr_Cy = 7, glr_Cz = 8;
