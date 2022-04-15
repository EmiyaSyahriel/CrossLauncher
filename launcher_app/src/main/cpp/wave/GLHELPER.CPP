/**
 * Created by EmiyaSyahriel, 20/06/2021.
 * OpenGL Utilities
 */
#include <cstdlib>
#include "WAVE.HPP"
#include "malloc.h"

void cxl_gl_check_error(int const line, const char* const file) {
    if (wave_consts.disable_glerr) return;
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

void cxl_gl_enable_blend() {
    glEnable(GL_BLEND);
    glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
}

void cxl_gl_disable_blend() {
    cxl_gl_reset_buffer();
    glBlendFunc(GL_ONE, GL_ZERO);
    glDisable(GL_BLEND);
    glUseProgram(0);
}

bool validate_shader(GLuint p, bool is_shader){
    GLint retval = 0;
    bool valid = true;
    (is_shader ? glGetShaderiv : glGetProgramiv)(p, is_shader ? GL_COMPILE_STATUS : GL_LINK_STATUS, &retval);
    if(retval != GL_TRUE){
        char* cpbuffer =(char*) malloc(sizeof(char) * SHADERCHECK_BUFSZ);
        int cpbufsz = SHADERCHECK_BUFSZ;
        (is_shader ? glGetShaderInfoLog : glGetProgramInfoLog)(p, SHADERCHECK_BUFSZ, &cpbufsz, cpbuffer);
        Log_e("Shader %s error : %s", is_shader ? "Compilation" : "Linking", cpbuffer);
        free(cpbuffer);
        valid = false;
    }
    return valid;
}

GLuint cxl_gl_compile_shader(const char* vertex, const char* fragment){
    GLuint vs = glCreateShader(GL_VERTEX_SHADER); CGL();
    GLuint fs = glCreateShader(GL_FRAGMENT_SHADER); CGL();
    GLuint ps = glCreateProgram(); CGL();
    glShaderSource(vs,1, &vertex, nullptr); CGL();
    glShaderSource(fs,1, &fragment, nullptr); CGL();
    glCompileShader(vs);CGL();
    glCompileShader(fs);CGL();
    validate_shader(vs, true);CGL();
    validate_shader(fs, true);CGL();
    glAttachShader(ps, vs);CGL();
    glAttachShader(ps, fs);CGL();
    glLinkProgram(ps);CGL();
    validate_shader(ps, false);CGL();
    return ps;
}

inline void cxl_gl_reset_buffer(){
    glBindBuffer(GL_ARRAY_BUFFER, 0);CGL();
    glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);CGL();
}

void cxl_gl_write_buffer(size_t const vsize, size_t const isize, GLuint const vbuffer, GLuint const ibuffer, const GLfloat *vdata, const GLuint *idata, GLenum drawmode)
{
    glBindBuffer(GL_ARRAY_BUFFER, vbuffer);CGL();
    glBufferData(GL_ARRAY_BUFFER, vsize *  sizeof(GLfloat), vdata, drawmode);CGL();
    glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ibuffer);CGL();
    glBufferData(GL_ELEMENT_ARRAY_BUFFER, isize * sizeof(GLint), idata, drawmode);CGL();
    cxl_gl_reset_buffer();
}

mat4 cxl_wave_matrix() {
    float scaleX = (float)wave_state.width / (float)wave_state.height;
    float scaleY = (float)wave_state.height / (float)wave_state.width;
    if (scaleX > scaleY) {
        return glm::ortho(-1.0f, 1.0f, -scaleY, scaleY, -10.0f, 10.0f);
    }
    else {
        return glm::ortho(-scaleX, scaleX, -1.0f, 1.0f, -10.0f, 10.0f);
    }
}

