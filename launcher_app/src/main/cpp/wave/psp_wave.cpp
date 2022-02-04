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

        gltWriteBuffer(wave_vdata_size, wave_index_size, vtbuff_wave, idbuff_wave, vdata.data(), idata.data(), GL_STATIC_DRAW);
        last_wave_type = wave_type;
    }
}

void psp_generate_buffers(){
    GLuint tmpBuffers[4];
    glGenBuffers(4, tmpBuffers);
    vtbuff_bg = tmpBuffers[0];
    vtbuff_wave = tmpBuffers[1];
    idbuff_bg = tmpBuffers[2];
    idbuff_wave = tmpBuffers[3];

    // fill out background buffer
    gltWriteBuffer(16, 6, vtbuff_bg, idbuff_bg, vtx_background_vdata, vtx_background_index, GL_STATIC_DRAW);
    psp_update_buffer_when_mode_changes();
}

void psp_compile_shader() {
    shader_bg = gltCompileShader(R::ps3_background_vert, R::ps3_background_frag);
    shader_wave = gltCompileShader(R::psp_wave_vert, R::psp_wave_frag);

    vunif_bg_ColorA = glGetUniformLocation(shader_bg, shuColorA);
    vunif_bg_ColorB = glGetUniformLocation(shader_bg, shuColorB);
    vunif_bg_ColorC = glGetUniformLocation(shader_bg, shuColorC);
    vunif_bg_TimeOfDay = glGetUniformLocation(shader_bg, shuTimeOfDay);
    vunif_bg_Month = glGetUniformLocation(shader_bg, shuMonth);
    vattr_bg_Position = glGetAttribLocation(shader_bg, shaPosition);
    vattr_bg_TexCoord = glGetAttribLocation(shader_bg, shaTexCoord);

    vunif_wave_Time = glGetUniformLocation(shader_wave, shuTime);
    vunif_wave_Ortho = glGetUniformLocation(shader_wave, shuOrtho);
    vunif_wave_ColorA = glGetUniformLocation(shader_wave, shuColorA);
    vunif_wave_ColorB = glGetUniformLocation(shader_wave, shuColorB);
    vattr_wave_Position = glGetAttribLocation(shader_wave, shaPosition);
    vattr_wave_VtxTimeA = glGetAttribLocation(shader_wave, shaVtxTimeA);


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
    glUseProgram(shader_wave);
    glBindBuffer(GL_ARRAY_BUFFER, vtbuff_wave); CGL();
    glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, idbuff_wave); CGL();

    // glUniform1f(vunif_wave_NormalStep, 0.01f); CGL();

    glm::vec4 cA = glm::vec4(1.0, 1.0f, 1.0f, 0.5f);
    glm::vec4 cB = glm::vec4(1.0, 1.0f, 1.0f, 0.0f);

    glUniform4f(vunif_wave_ColorA, cA.r, cA.g, cA.b, cA.a); CGL();
    glUniform4f(vunif_wave_ColorB, cB.r, cB.g, cB.b, cB.a); CGL();

    glm::mat4 matrix = psp_wave_matrix(); CGL();
    glUniformMatrix4fv(vunif_wave_Ortho, 1, GL_FALSE, &matrix[0][0]); CGL();

    glVertexAttribPointer(vattr_wave_Position, 3, GL_FLOAT, GL_FALSE, 6 * sizeof(GLfloat), (GLvoid*)(0 * sizeof(GLfloat))); CGL();
    glVertexAttribPointer(vattr_wave_VtxTimeA, 3, GL_FLOAT, GL_FALSE, 6 * sizeof(GLfloat), (GLvoid*)(3 * sizeof(GLfloat))); CGL();
    glEnableVertexAttribArray(vattr_wave_Position); CGL();
    glEnableVertexAttribArray(vattr_wave_VtxTimeA); CGL();
    glUniform1f(vunif_wave_Time, currentTime * xmb_wave_speed); CGL();
    glDrawElements(GL_TRIANGLES, wave_index_size, GL_UNSIGNED_INT, nullptr); CGL();
    glUniform1f(vunif_wave_Time, (currentTime + 7.35f) * 0.95 * xmb_wave_speed); CGL();
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
    glDeleteProgram(shader_bg); CGL();
    glDeleteProgram(shader_wave); CGL();

    GLuint delBuffer[4] = {
            vtbuff_bg, vtbuff_wave,
            idbuff_bg, idbuff_wave
    }; CGL();
    glDeleteBuffers(4, delBuffer); CGL();
}