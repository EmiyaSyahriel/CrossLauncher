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
            case GL_STACK_UNDERFLOW_KHR: err_str = "STACK_UNDERFLOW_KHR"; break;
            case GL_STACK_OVERFLOW_KHR: err_str = "STACK_OVERFLOW_KHR"; break;
            default: err_str = "UNKNOWN_GL_ERROR"; break;
        }
        Log_e("GL_ERROR[%i] (%s) %s:%i ", err, err_str, file, line);
    }
}

inline void gltPrintCompileError(GLuint shader){
    GLint status = GL_TRUE;
    glGetShaderiv(shader, GL_LINK_STATUS, &status);
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
    glCompileShader(vs);
    glCompileShader(fs);
    gltPrintCompileError(vs);
    gltPrintCompileError(fs);
    glAttachShader(ps, vs);CGL();
    glAttachShader(ps, fs);CGL();
    glLinkProgram(ps);CGL();
    gltPrintLinkError(ps);
    return ps;
}