
#include "wave_gl.h"
#include "gl.h"
#include "Logger.h"
#include <malloc.h>
#include <vector>

#if defined(__GNUC__) || defined(ANDROID)
#define ginline __attribute__((always_inline)) inline
#elif defined(_MSC_VER)
#define ginline __forceinline inline
#else
#define ginline inline
#endif

#define trigger_bkpt() // raise(SIGTRAP);

GLuint shprog_dumm, shprog_back, shprog_wave;
GLuint vshadr_dumm, vshadr_back, vshadr_wave;
GLuint fshadr_dumm, fshadr_back, fshadr_wave;
GLuint ibuffr_dumm, ibuffr_back, ibuffr_wave;
GLuint vbuffr_dumm, vbuffr_back, vbuffr_wave;
GLint wave_attr_vpos = -1, wave_unif_time = -1, wave_unif_yscl = -1, wave_unif_nrst = -1;
GLint back_attr_vpos = -1, back_attr_vtex = -1, back_unif_cola = -1, back_unif_colb = -1;
float screen_width, screen_height, wave_y_scale;

bool wave_paused = false;

void check_gl_error(const char* source, int line){
}

template<typename T> ginline void copy_to_heap (std::vector<T> vec, T* target){
    size_t tsize = sizeof(T) * vec.size();
    target = (T*)calloc(vec.size(), sizeof(T));
    (T*)memcpy(target, vec.data(), tsize);
    trigger_bkpt();
}

#define stack2heap(T, vec, target) \
size_t tsize = sizeof(T) * vec.size();\
target = (T*)malloc(tsize);\
(T*) memcpy(target, vec.data(), tsize); \
trigger_bkpt();

const uint  background_idata[4] = {0,1,2,3};
const float background_vdata[8] = { -1.0f, -1.0f, 1.0f, -1.0f, -1.0f,  1.0f, 1.0f,  1.0f };

uint* wave_idata;
float* wave_vdata;
int wave_vtx_count;
int wave_v3_scalar_count;
#define wave_elem_count (wave_vtx_count / 2)

int wave_grid_size = 16;

ginline void ensure_cpu_wave_data(){
    if(wave_idata == nullptr){
        std::vector<uint> idata_tmp = std::vector<uint>();

        for(int y = 0; y < wave_grid_size - 1; y++){
            for(int x = 0; x < wave_grid_size - 1; x++) {
                int i = (y * wave_grid_size) + x;
                idata_tmp.push_back(i);
                idata_tmp.push_back(i+1);
                idata_tmp.push_back(i+wave_grid_size);
                idata_tmp.push_back(i+wave_grid_size+1);
            }
        }
        wave_vtx_count = idata_tmp.size();
        stack2heap(uint, idata_tmp, wave_idata);
    }

    if(wave_vdata == nullptr){
        std::vector<float> vdata_tmp = std::vector<float>();
        for(int y = 0; y < wave_grid_size; y++){
            for(int x = 0; x < wave_grid_size; x++) {
                vdata_tmp.push_back((((float)x / (float)wave_grid_size) * 2.0f) - 1.0f);
                vdata_tmp.push_back((((float)y / (float)wave_grid_size) * 2.0f) - 1.0f);
                vdata_tmp.push_back(0.0f);
            }
        }
        wave_v3_scalar_count = vdata_tmp.size();
        stack2heap(float, vdata_tmp, wave_vdata);
    }
}

ginline bool glbuffer_should_regen(GLuint *idata){
    bool retval = !glIsBuffer(*idata); CGL();
    if(retval) {
        glGenBuffers(1, idata); CGL();
        Log_w("Buffer regenerated as %08x", *idata);
    }
    return !retval; // should rebuild
}

ginline bool shader_should_relink(GLuint *idata){
    bool retval = glIsProgram(*idata); CGL();
    if(!retval) { // Not program, new entrypoint
        *idata = glCreateProgram(); CGL();
        Log_w("new GLProgram object created");
    }
    return !retval; // should rebuild;
}

ginline bool shader_should_recompile(GLuint *idata, GLenum type){
    bool retval = glIsShader(*idata); CGL();
    if(!retval) {
        *idata = glCreateShader(type); CGL();
        Log_w("new GLShader object created");
    }
    return !retval;
}

ginline void ensure_gl_data_exe(GLuint* v, GLuint* f, GLuint *p, char* vspath, char* fspath){
    if(     shader_should_recompile(v, GL_VERTEX_SHADER) ||
            shader_should_recompile(f, GL_FRAGMENT_SHADER) ||
            shader_should_relink(p)
            ) {
        wave_compile(*v, vspath); CGL();
        wave_compile(*f, fspath); CGL();
        wave_link(*p, *v, *f); CGL();
    }
}

ginline void ensure_gl_attr(GLuint p, GLint *a, const char* name){
    if(*a == -1 && glIsProgram(p)) { CGL();
        *a = glGetAttribLocation(p, name);CGL();
    }
}

ginline void ensure_gl_unif(GLuint p, GLint *a, const char* name){
    if(*a == -1 && glIsProgram(p)) { CGL();
        *a = glGetUniformLocation(p, name); CGL();
    }
}

void wave_compile(uint prog, char* src){
    glShaderSource(prog, 1, &src, nullptr); CGL();
    glCompileShader(prog); CGL();

    int compile_status = GL_FALSE;
    glGetShaderiv(prog, GL_COMPILE_STATUS, &compile_status); CGL();
    if(compile_status == GL_FALSE){
        int buflen = 512;
        char* logbuf = (char*)malloc(buflen);
        glGetShaderInfoLog(prog, buflen, &buflen, logbuf); CGL();
        // logbuf = (char*)realloc(logbuf, buflen);
        Log_e("Fail to compile \"GLS_%08x.glsl.o\" : %s",prog, logbuf);
        free(logbuf);
        glDeleteShader(prog); CGL();
    }else{
        Log_d("Succesfully compiled \"GLS_%08x.glsl.o\"", prog);
    }

    free(src);
}

void wave_ensure_needed_data(){
    ensure_gl_data_exe(&vshadr_back, &fshadr_back, &shprog_back, (char *) "xmb_background.vert",(char *) "xmb_background.frag"); CGL();
    ensure_gl_data_exe(&vshadr_wave, &fshadr_wave, &shprog_wave, (char *) "xmb_wave.vert",(char *) "xmb_wave.frag"); CGL();
    ensure_gl_data_exe(&vshadr_dumm, &fshadr_dumm, &shprog_dumm, (char *) "blank.vert",(char *) "blank.frag"); CGL();

    ensure_gl_attr(shprog_wave, &wave_attr_vpos, "position"); CGL();
    ensure_gl_unif(shprog_wave, &wave_unif_time, "_Time"); CGL();
    ensure_gl_unif(shprog_wave, &wave_unif_yscl, "_YScale"); CGL();
    ensure_gl_unif(shprog_wave, &wave_unif_nrst, "_NormalStep"); CGL();

    ensure_gl_attr(shprog_back, &back_attr_vpos, "vpos"); CGL();
    ensure_gl_attr(shprog_back, &back_attr_vtex, "uv"); CGL();
    ensure_gl_unif(shprog_back, &back_unif_cola, "_ColorA"); CGL();
    ensure_gl_unif(shprog_back, &back_unif_colb, "_ColorB"); CGL();

    if(glbuffer_should_regen(&ibuffr_back)){
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ibuffr_back); CGL();
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, sizeof(uint) * 4, background_idata, GL_STATIC_DRAW); CGL();
    }

    if(glbuffer_should_regen(&vbuffr_back)){
        glBindBuffer(GL_ARRAY_BUFFER, vbuffr_back); CGL();
        glBufferData(GL_ARRAY_BUFFER, sizeof(float) * 8, background_vdata, GL_STATIC_DRAW); CGL();
    }

    if(glbuffer_should_regen(&ibuffr_wave)){
        ensure_cpu_wave_data(); CGL();
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ibuffr_wave); CGL();
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, sizeof(uint) * wave_vtx_count, wave_idata, GL_DYNAMIC_DRAW); CGL();
    }

    if(glbuffer_should_regen(&vbuffr_wave)){
        ensure_cpu_wave_data(); CGL();
        glBindBuffer(GL_ARRAY_BUFFER, vbuffr_wave); CGL();
        glBufferData(GL_ARRAY_BUFFER, sizeof(float) * wave_v3_scalar_count, wave_vdata, GL_DYNAMIC_DRAW); CGL();
    }
}

void wave_resize(int w, int h){
    screen_width = (float)w;
    screen_height = (float)h;
    wave_y_scale = screen_height / screen_width;
    glViewport(0,0,w,h);
    Log_d("Resized : %ix%i",w,h);
}

void wave_link(uint prog, uint vs, uint fs){

    bool isVShader = glIsShader(vs);
    bool isFShader = glIsShader(fs);
    bool isPShader = glIsProgram(prog);

    glAttachShader(prog, vs); CGL();
    glAttachShader(prog, fs); CGL();
    glLinkProgram(prog); CGL();

    int compile_status = GL_FALSE;
    glGetProgramiv(prog, GL_LINK_STATUS, &compile_status);

    int buflen = 512;
    char* logbuf = (char*)malloc(buflen);
    glGetProgramInfoLog(prog, buflen, &buflen, logbuf); CGL();
    logbuf = (char*)realloc(logbuf, buflen);
    Log_e("Error Buffer : %s", logbuf);
    free(logbuf);

    if(compile_status == GL_FALSE){
        Log_e("Failed to link (%i,%i)->(%i)", vs,fs,prog);
        glDeleteProgram(prog); CGL();
    }else{
        Log_d("Successfully linked shader(%i, %i) to PROG_%i", vs,fs,prog);
    }
}

void wave_start(){
    wave_ensure_needed_data();
    wave_resize(10,10); CGL();
    glClearColor(0.6f,0.0f,1.0f,1.0f); CGL();
}

float current_time = 0.0f;

void wave_draw(float ms){
    wave_ensure_needed_data();

    glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);CGL();
    glEnable(GL_BLEND);CGL();
    glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);CGL();

    current_time += ms;

    if(current_time > 65535.0f) current_time = 0.0f;CGL();

    glUseProgram(shprog_back);CGL();
    glBindBuffer(GL_ARRAY_BUFFER, vbuffr_back);CGL();
    glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ibuffr_back);CGL();
    glVertexAttribPointer(back_attr_vpos, 2, GL_FLOAT, GL_FALSE, 0 * sizeof(float), nullptr);CGL();
    glVertexAttribPointer(back_attr_vtex, 2, GL_FLOAT, GL_FALSE, 2 * sizeof(float), nullptr);CGL();

    glUseProgram(shprog_wave);CGL();
    glUniform1f(wave_unif_time, current_time);CGL();
    glUniform1f(wave_unif_yscl, wave_y_scale);CGL();
    glUniform1f(wave_unif_nrst, 0.02f);CGL();
    glBindBuffer(GL_ARRAY_BUFFER, vbuffr_wave);CGL();
    glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ibuffr_wave);CGL();
    glVertexAttribPointer(wave_attr_vpos, 3, GL_FLOAT, GL_FALSE, 0, nullptr);CGL();
    glDrawElements(GL_TRIANGLE_STRIP, wave_elem_count, GL_UNSIGNED_INT, nullptr);CGL();

    glFlush();CGL();
}

void wave_kill() {
    if(glIsProgram(shprog_back)) glDeleteProgram(shprog_back);
    if(glIsProgram(shprog_wave)) glDeleteProgram(shprog_wave);
    if(glIsProgram(shprog_dumm)) glDeleteProgram(shprog_dumm);

    if(glIsBuffer(ibuffr_back)) glDeleteBuffers(1, &ibuffr_back);
    if(glIsBuffer(ibuffr_wave)) glDeleteBuffers(1, &ibuffr_back);
    if(glIsBuffer(ibuffr_dumm)) glDeleteBuffers(1, &ibuffr_back);

    if(glIsBuffer(vbuffr_back)) glDeleteBuffers(1, &vbuffr_back);
    if(glIsBuffer(vbuffr_wave)) glDeleteBuffers(1, &vbuffr_back);
    if(glIsBuffer(vbuffr_dumm)) glDeleteBuffers(1, &vbuffr_back);
    free(wave_vdata);
    free(wave_idata);
}
