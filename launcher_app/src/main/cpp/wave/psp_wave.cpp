#include "psp_wave.h"
#include "gl.h"
#include "typedefs.h"
#include "states.h"
#include "textures.h"
#include "shaders.h"
#include "Logger.h"
#include <vector>

void ps3_draw_background();

WAVE_TYPE last_wave_type = WAVE_TYPE::DEFAULT;

#ifndef PSP_FILL_VDATA
#define PSP_FILL_VDATA(x,y,z,alpha,x_step,apply_wave) \
vdata.push_back(x);     vdata.push_back(y);      vdata.push_back(z);\
vdata.push_back(alpha); vdata.push_back(x_step); vdata.push_back(apply_wave)
#endif

#ifndef PSP_DETAIL_SIZE
#define PSP_DETAIL_SIZE (xmb_detail_size)
#endif

#ifndef PSP_VTI
#define PSP_VTI(x, y, o) ((x + o) + (PSP_DETAIL_SIZE * y))
#endif

#ifndef PSP_FILL_IDATA_TRI
#define PSP_FILL_IDATA_TRI(x,y,z) \
idata.push_back(x);\
idata.push_back(y);\
idata.push_back(z);
#endif

#ifndef PSP_FILL_IDATA_1L
#define PSP_FILL_IDATA_1L(x) \
PSP_FILL_IDATA_TRI(PSP_VTI(x, 0, 0),PSP_VTI(x, 0, 1),PSP_VTI(x, 1, 0) );\
PSP_FILL_IDATA_TRI(PSP_VTI(x, 0, 1),PSP_VTI(x, 1, 0),PSP_VTI(x, 1, 1) );\
PSP_FILL_IDATA_TRI(PSP_VTI(x, 1, 0),PSP_VTI(x, 1, 1),PSP_VTI(x, 2, 0) );\
PSP_FILL_IDATA_TRI(PSP_VTI(x, 1, 1),PSP_VTI(x, 2, 0),PSP_VTI(x, 2, 1) );\
PSP_FILL_IDATA_TRI(PSP_VTI(x, 2, 0),PSP_VTI(x, 2, 1),PSP_VTI(x, 3, 0) );\
PSP_FILL_IDATA_TRI(PSP_VTI(x, 2, 1),PSP_VTI(x, 3, 0),PSP_VTI(x, 3, 1) )
#endif

#ifndef PSP_FILL_IDATA_2L
#define PSP_FILL_IDATA_2L(x) \
PSP_FILL_IDATA_TRI(PSP_VTI(x, 0, 0),PSP_VTI(x, 0, 1),PSP_VTI(x, 1, 0) );\
PSP_FILL_IDATA_TRI(PSP_VTI(x, 0, 1),PSP_VTI(x, 1, 0),PSP_VTI(x, 1, 1) );\
PSP_FILL_IDATA_TRI(PSP_VTI(x, 1, 0),PSP_VTI(x, 1, 1),PSP_VTI(x, 2, 0) );\
PSP_FILL_IDATA_TRI(PSP_VTI(x, 1, 1),PSP_VTI(x, 2, 0),PSP_VTI(x, 2, 1) );\
PSP_FILL_IDATA_TRI(PSP_VTI(x, 2, 0),PSP_VTI(x, 2, 1),PSP_VTI(x, 3, 0) );\
PSP_FILL_IDATA_TRI(PSP_VTI(x, 2, 1),PSP_VTI(x, 3, 0),PSP_VTI(x, 3, 1) );\
PSP_FILL_IDATA_TRI(PSP_VTI(x, 3, 0), PSP_VTI(x, 3, 1), PSP_VTI(x, 4, 0)); \
PSP_FILL_IDATA_TRI(PSP_VTI(x, 3, 1), PSP_VTI(x, 4, 0), PSP_VTI(x, 4, 1)); \
PSP_FILL_IDATA_TRI(PSP_VTI(x, 4, 0), PSP_VTI(x, 4, 1), PSP_VTI(x, 5, 0)); \
PSP_FILL_IDATA_TRI(PSP_VTI(x, 4, 1), PSP_VTI(x, 5, 0), PSP_VTI(x, 5, 1)); \
PSP_FILL_IDATA_TRI(PSP_VTI(x, 5, 0), PSP_VTI(x, 5, 1), PSP_VTI(x, 6, 0)); \
PSP_FILL_IDATA_TRI(PSP_VTI(x, 5, 1), PSP_VTI(x, 6, 0), PSP_VTI(x, 6, 1))
#endif

#ifndef PSP_DETAIL_I 
#define PSP_DETAIL_I ((float)i / (float)(PSP_DETAIL_SIZE - 1))
#endif

#ifndef PSP_FOREACH_DETAIL
#define PSP_FOREACH_DETAIL(cmd) for (i = 0; i < PSP_DETAIL_SIZE; i++) {  \
x = (PSP_DETAIL_I * 2.0) - 1.0; cmd\
}
#endif

void psp_update_buffer_when_mode_changes() {
    if (last_wave_type != wave_type) {
        std::vector<GLfloat> vdata;
        std::vector<GLuint> idata;
        float x, xs;
        int i;
        if (wave_type == WAVE_TYPE::PSP_BOTTOM) {
            PSP_FOREACH_DETAIL({ PSP_FILL_VDATA(x,  0.000f, 0.0, 1.00, PSP_DETAIL_I, 1.0); });
            PSP_FOREACH_DETAIL({ PSP_FILL_VDATA(x, -0.031f, 0.0, 0.60, PSP_DETAIL_I, 1.0); });
            PSP_FOREACH_DETAIL({ PSP_FILL_VDATA(x, -0.250f, 0.0, 0.00, PSP_DETAIL_I, 1.0); });
            PSP_FOREACH_DETAIL({ PSP_FILL_VDATA(x, -3.0f, 0.0, 0.0, 0.0, 0.0); });
            PSP_FOREACH_DETAIL({
                if ((i + 1) < PSP_DETAIL_SIZE) {
                    PSP_FILL_IDATA_1L(i);
                }; 
                });
        }
        else if (wave_type == WAVE_TYPE::PSP_CENTER) {
            PSP_FOREACH_DETAIL({ PSP_FILL_VDATA(x,  0.250f, 0.0, 1.00, 0.000 + PSP_DETAIL_I, 1.0); });
            PSP_FOREACH_DETAIL({ PSP_FILL_VDATA(x,  0.200f, 0.0, 0.60, 0.000 + PSP_DETAIL_I, 1.0); });
            PSP_FOREACH_DETAIL({ PSP_FILL_VDATA(x,  0.050f, 0.0, 0.00, 0.000 + PSP_DETAIL_I, 0.5); });
            PSP_FOREACH_DETAIL({ PSP_FILL_VDATA(x,  0.000f, 0.0, 0.00, 1.375, 0.25); });
            PSP_FOREACH_DETAIL({ PSP_FILL_VDATA(x, -0.050f, 0.0, 0.00, 2.750 + PSP_DETAIL_I, 0.5); });
            PSP_FOREACH_DETAIL({ PSP_FILL_VDATA(x, -0.200f, 0.0, 0.60, 2.750 + PSP_DETAIL_I, 1.0); });
            PSP_FOREACH_DETAIL({ PSP_FILL_VDATA(x, -0.250f, 0.0, 1.00, 2.750 + PSP_DETAIL_I, 1.0); });
            PSP_FOREACH_DETAIL({
                if ((i + 1) < PSP_DETAIL_SIZE) {
                    PSP_FILL_IDATA_2L(i);
                };
                });
        }

        wave_vdata_size = vdata.size();
        wave_index_size = idata.size();

        gltWriteBuffer(wave_vdata_size, wave_index_size, wave.vtbuf, wave.idbuf, vdata.data(), idata.data(), GL_STATIC_DRAW);
        last_wave_type = wave_type;
    }
}

void psp_generate_buffers(){
    GLuint tmpBuffers[4];
    glGenBuffers(4, tmpBuffers);
    bg  .vtbuf = tmpBuffers[0];
    wave.vtbuf = tmpBuffers[1];
    bg  .idbuf = tmpBuffers[2];
    wave.idbuf = tmpBuffers[3];

    // fill out background buffer
    gltWriteBuffer(16, 6, bg.vtbuf, bg.idbuf, vtx_background_vdata, vtx_background_index, GL_STATIC_DRAW);
    psp_update_buffer_when_mode_changes();
}

void psp_compile_shader() {
    bg.shader = gltCompileShader(R::ps3_background_vert, R::ps3_background_frag);
    wave.shader = gltCompileShader(R::psp_wave_vert, R::psp_wave_frag);

    bg_unif.colorA    = glGetUniformLocation(bg.shader, shader_unif_name.colorA);
    bg_unif.colorB    = glGetUniformLocation(bg.shader, shader_unif_name.colorB);
    bg_unif.colorC    = glGetUniformLocation(bg.shader, shader_unif_name.colorC);
    bg_unif.timeOfDay = glGetUniformLocation(bg.shader, shader_unif_name.timeOfDay);
    bg_unif.month     = glGetUniformLocation(bg.shader, shader_unif_name.month);
    bg_attr.position  = glGetAttribLocation (bg.shader, shader_attr_name.position);
    bg_attr.texCoord  = glGetAttribLocation (bg.shader, shader_attr_name.texCoord);

    wave_unif.time     = glGetUniformLocation(wave.shader, shader_unif_name.time);
    wave_unif.ortho    = glGetUniformLocation(wave.shader, shader_unif_name.ortho);
    wave_unif.colorA   = glGetUniformLocation(wave.shader, shader_unif_name.colorA);
    wave_unif.colorB   = glGetUniformLocation(wave.shader, shader_unif_name.colorB);
    wave_attr.position = glGetAttribLocation (wave.shader, shader_attr_name.position);
    wave_attr.vtxTimeA = glGetAttribLocation (wave.shader, shader_attr_name.vtxTimeA);


}

void psp_start(){
    psp_compile_shader();
    psp_generate_buffers();
    load_texture(siang_shades, &tex_day);
    load_texture(malam_shades, &tex_night);
}

void psp_resize(float w, float h){
    glViewport(0,0,(int)w,(int)h);
    xmb_screen_w = (int)w;
    xmb_screen_h = (int)h;
}

void psp_wave_setup_blend(){
    glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
    glEnable(GL_BLEND);
    glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
}

glm::mat4 psp_wave_matrix() {
    float scaleX = (float)xmb_screen_w / (float)xmb_screen_h;
    float scaleY = (float)xmb_screen_h / (float)xmb_screen_w;
    if (scaleX > scaleY) {
        return glm::ortho(-1.0f, 1.0f, -scaleY, scaleY, -10.0f, 10.0f);
    }
    else {
        return glm::ortho(-scaleX, scaleX, -1.0f, 1.0f, -10.0f, 10.0f);
    }
}

void psp_wave_unsetup_blend(){
    gltResetBoundBuffer();
    glBlendFunc(GL_ONE, GL_ZERO);
    glDisable(GL_BLEND);
    glUseProgram(0);
}

void psp_draw_background(){
    // Call to PS3's
    ps3_draw_background();
}

void psp_draw_wave(){
    glUseProgram(wave.shader);
    glBindBuffer(GL_ARRAY_BUFFER, wave.vtbuf); CGL();
    glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, wave.idbuf); CGL();

    // glUniform1f(vunif_wave_NormalStep, 0.01f); CGL();

    glm::vec4 cA = glm::vec4(1.0, 1.0f, 1.0f, 0.5f);
    glm::vec4 cB = glm::vec4(1.0, 1.0f, 1.0f, 0.0f);

    glUniform4f(wave_unif.colorA, cA.r, cA.g, cA.b, cA.a); CGL();
    glUniform4f(wave_unif.colorB, cB.r, cB.g, cB.b, cB.a); CGL();

    glm::mat4 matrix = psp_wave_matrix(); CGL();
    glUniformMatrix4fv(wave_unif.ortho, 1, GL_FALSE, &matrix[0][0]); CGL();

    glVertexAttribPointer(    wave_attr.position, 3, GL_FLOAT, GL_FALSE, 6 * sizeof(GLfloat), (GLvoid*)(0 * sizeof(GLfloat))); CGL();
    glVertexAttribPointer(    wave_attr.vtxTimeA, 3, GL_FLOAT, GL_FALSE, 6 * sizeof(GLfloat), (GLvoid*)(3 * sizeof(GLfloat))); CGL();
    glEnableVertexAttribArray(wave_attr.position); CGL();
    glEnableVertexAttribArray(wave_attr.vtxTimeA); CGL();
    glUniform1f(wave_unif.time, currentTime * xmb_wave_speed); CGL();
    glDrawElements(GL_TRIANGLES, wave_index_size, GL_UNSIGNED_INT, nullptr); CGL();
    glUniform1f(wave_unif.time, (currentTime + 7.35f) * 0.95 * xmb_wave_speed); CGL();
    glDrawElements(GL_TRIANGLES, wave_index_size, GL_UNSIGNED_INT, nullptr); CGL();
}

void psp_draw(float ms){
    if(!HAS_FLAG(wave_type, WAVE_TYPE::PSP, int8_t)) return;

    currentTime += ms;
    if (currentTime >= 100000.0f)  currentTime = 0.0f;

    psp_wave_setup_blend();

    psp_draw_background();
    psp_update_buffer_when_mode_changes();
    psp_draw_wave();

    psp_wave_unsetup_blend();
}

void psp_destroy(){
    glDeleteProgram(bg.shader); CGL();
    glDeleteProgram(wave.shader); CGL();

    GLuint delBuffer[4] = {
            bg.vtbuf, wave.vtbuf,
            bg.idbuf, wave.idbuf
    }; CGL();
    glDeleteBuffers(4, delBuffer); CGL();
}