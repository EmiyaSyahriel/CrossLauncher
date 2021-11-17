#include <cstdlib>
#include "gl.h"
#include "Logger.h"

void hqbdjknsvvbdjksdsvbjkvdjkls_cglerr(int const line, const char* const file) {
    GLenum err = glGetError();

    if(err != GL_NO_ERROR){
        const char* err_str = "";
        switch(err){
            case GL_INVALID_ENUM: err_str = "INVALID_ENUM"; break;
            case GL_INVALID_VALUE: err_str = "INVALID_VALUE"; break;
            case GL_INVALID_OPERATION: err_str = "INVALID_OPERATION"; break;
            case GL_INVALID_FRAMEBUFFER_OPERATION: err_str = "INVALID_FRAMEBUFFER_OPERATION"; break;
            case GL_OUT_OF_MEMORY: err_str = "OUT_OF_MEMORY"; break;
            default: err_str = "UNKNOWN_GL_ERROR"; break;
        }
        Log_e("GL_ERROR[%i] (%s) %s:%i ", err, err_str, file, line);
    }
}

inline void gltPrintCompileError(GLuint shader){
    GLint status = GL_TRUE;
    glGetShaderiv(shader, GL_COMPILE_STATUS, &status);
    if(status != GL_TRUE){
        int bufferSize = 512;
        char* buffer = new char[bufferSize];
        glGetShaderInfoLog(shader, bufferSize, &bufferSize, buffer);
        Log_e("GL_%08x.glsl.o : %s", shader, buffer);
        delete[] buffer;
    }
}

inline void gltPrintLinkError(GLuint program){
    GLint status = GL_TRUE;
    glGetProgramiv(program, GL_LINK_STATUS, &status);
    if(status != GL_TRUE){
        int bufferSize = 512;
        char* buffer = new char[bufferSize];
        glGetProgramInfoLog(program, bufferSize, &bufferSize, buffer);
        Log_e("GL_%08x.exe : %s", program, buffer);
        delete[] buffer;
    }
}

GLuint gltCompileShader(const char* vertex, const char* fragment){
    GLuint vs = glCreateShader(GL_VERTEX_SHADER); CGL();
    GLuint fs = glCreateShader(GL_FRAGMENT_SHADER); CGL();
    GLuint ps = glCreateProgram(); CGL();
    glShaderSource(vs,1, &vertex, nullptr); CGL();
    glShaderSource(fs,1, &fragment, nullptr); CGL();
    glCompileShader(vs);CGL();
    glCompileShader(fs);CGL();
    gltPrintCompileError(vs);CGL();
    gltPrintCompileError(fs);CGL();
    glAttachShader(ps, vs);CGL();
    glAttachShader(ps, fs);CGL();
    glLinkProgram(ps);CGL();
    gltPrintLinkError(ps);CGL();
    return ps;
}

inline void gltResetBoundBuffer(){
    glBindBuffer(GL_ARRAY_BUFFER, 0);CGL();
    glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);CGL();
}


void gltWriteBuffer(int const vsize, int const isize, GLuint const vbuffer, GLuint const ibuffer, GLfloat *vdata, GLuint *idata, GLenum drawmode)
{
    glBindBuffer(GL_ARRAY_BUFFER, vbuffer);CGL();
    glBufferData(GL_ARRAY_BUFFER, vsize *  sizeof(GLfloat), vdata, drawmode);CGL();
    glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ibuffer);CGL();
    glBufferData(GL_ELEMENT_ARRAY_BUFFER, isize * sizeof(GLint), idata, drawmode);CGL();
    gltResetBoundBuffer();
}

void gltDrawBuffer(GLuint const vbuffer, GLuint const ibuffer, int vcount)
{
    glBindBuffer(GL_ARRAY_BUFFER, vbuffer);CGL();
    glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ibuffer);CGL();
    glDrawElements(GL_TRIANGLE_STRIP, vcount, GL_UNSIGNED_INT, nullptr);CGL();
    gltResetBoundBuffer();
}