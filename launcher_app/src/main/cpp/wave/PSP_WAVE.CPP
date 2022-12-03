/**
 * Created by EmiyaSyahriel on 18/05/2021.
 * PlayStation Portable Wave Renderer
 *
 * The Wave itself is procedurally calculated in the vertex shader, the generator codes actually
 * only generates vertices once the style changes to be used by the shader on every draw call.
 */
#include "WAVE.HPP"
#include <vector>

#ifndef PSP_FILL_VDATA
#define PSP_FILL_VDATA(x,y,z,alpha,x_step,apply_wave) \
vdata.push_back(x);     vdata.push_back(y);      vdata.push_back(z);\
vdata.push_back(alpha); vdata.push_back(x_step); vdata.push_back(apply_wave)
#endif

#ifndef PSP_DETAIL_SIZE
#define PSP_DETAIL_SIZE (wave_consts.detail_size)
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
    //if (last_wave_type != wave_type) {
        std::vector<GLfloat> vdata;
        std::vector<GLuint> idata;
        float x, xs;
        int i;
        if (wave_state.style == WAVE_STYLE::PSP_BOTTOM) {
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
        else if (wave_state.style == WAVE_STYLE::PSP_CENTER) {
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

        wave_state.psp_wave.vdata_sz = vdata.size();
        wave_state.psp_wave.idata_sz = idata.size();

        cxl_gl_write_buffer(wave_state.psp_wave.vdata_sz, wave_state.psp_wave.idata_sz, wave_state.psp_wave.vt, wave_state.psp_wave.id, vdata.data(), idata.data(), GL_STATIC_DRAW);
        wave_state.last_style = wave_state.style;
    //}
}

void psp_query_shader_interfaces() {
    wave_state.psp_wave.POSITION = glGetAttribLocation(wave_state.psp_wave.program, wave_consts.attr_names.POSITION); CGL();
    wave_state.psp_wave.TEXCOORD1 = glGetAttribLocation(wave_state.psp_wave.program, wave_consts.attr_names.TEXCOORD1); CGL();

    wave_state.psp_wave._Time =     glGetUniformLocation(wave_state.psp_wave.program,  wave_consts.unif_names._Time); CGL();
    wave_state.psp_wave._Ortho =    glGetUniformLocation(wave_state.psp_wave.program, wave_consts.unif_names._Ortho); CGL();
    wave_state.psp_wave._RngDataA = glGetUniformLocation(wave_state.psp_wave.program, wave_consts.unif_names._RngDataA); CGL();
    wave_state.psp_wave._RngDataB = glGetUniformLocation(wave_state.psp_wave.program, wave_consts.unif_names._RngDataB); CGL();
    wave_state.psp_wave._ColorA =   glGetUniformLocation(wave_state.psp_wave.program, wave_consts.unif_names._ColorA); CGL();
    wave_state.psp_wave._ColorB =   glGetUniformLocation(wave_state.psp_wave.program, wave_consts.unif_names._ColorB); CGL();
    wave_state.psp_wave._RngTrans = glGetUniformLocation(wave_state.psp_wave.program, wave_consts.unif_names._RngTrans); CGL();
    wave_state.psp_wave._YScale = glGetUniformLocation(wave_state.psp_wave.program, wave_consts.unif_names._YScale); CGL();
}

void cxl_psp_wave_start(){
    psp_update_buffer_when_mode_changes();
    psp_query_shader_interfaces();
}

glm::mat4 psp_wave_matrix() {
    float scaleX = (float)wave_state.width / (float)wave_state.height;
    float scaleY = (float)wave_state.height / (float)wave_state.width;
    if (scaleX > scaleY) {
        return glm::ortho(-1.0f, 1.0f, -scaleY, scaleY, -10.0f, 10.0f);
    }
    else {
        return glm::ortho(-scaleX, scaleX, -1.0f, 1.0f, -10.0f, 10.0f);
    }
}

void psp_draw_wave(){

    if (wave_state.last_style != wave_state.style) {
        wave_state.last_style = wave_state.style;
        psp_update_buffer_when_mode_changes();
    }

    glUseProgram(wave_state.psp_wave.program);
    glBindBuffer(GL_ARRAY_BUFFER, wave_state.psp_wave.vt); CGL();
    glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, wave_state.psp_wave.id); CGL();

    glm::vec4 cA = wave_state.colors.fg_a;
    glm::vec4 cB = wave_state.colors.fg_b;

    glUniform4f(wave_state.psp_wave._ColorA, cA.r, cA.g, cA.b, cA.a); CGL();
    glUniform4f(wave_state.psp_wave._ColorB, cB.r, cB.g, cB.b, cB.a); CGL();

    glm::mat4 matrix = psp_wave_matrix(); CGL();
    glUniformMatrix4fv(wave_state.psp_wave._Ortho, 1, GL_FALSE, &matrix[0][0]); CGL();
    glUniform1fv(wave_state.psp_wave._RngDataA, 6, wave_state.rng_data.A);
    glUniform1fv(wave_state.psp_wave._RngDataB, 6, wave_state.rng_data.B);
    glUniform1f(wave_state.psp_wave._RngTrans, wave_state.rng_data.t);
    glUniform1f(wave_state.psp_wave._YScale, wave_state.vert_scale); CGL();

    glVertexAttribPointer(    wave_state.psp_wave.POSITION, 3, GL_FLOAT, GL_FALSE, 6 * sizeof(GLfloat), (GLvoid*)(0 * sizeof(GLfloat))); CGL();
    glVertexAttribPointer(    wave_state.psp_wave.TEXCOORD1, 3, GL_FLOAT, GL_FALSE, 6 * sizeof(GLfloat), (GLvoid*)(3 * sizeof(GLfloat))); CGL();
    glEnableVertexAttribArray(wave_state.psp_wave.POSITION); CGL();
    glEnableVertexAttribArray(wave_state.psp_wave.TEXCOORD1); CGL();
    glUniform1f(wave_state.psp_wave._Time, wave_state.current_time); CGL();
    glDrawElements(GL_TRIANGLES, wave_state.psp_wave.idata_sz, GL_UNSIGNED_INT, nullptr); CGL();
    glUniform1f(wave_state.psp_wave._Time, ((float)wave_state.current_time + 7.35f) * 0.95f); CGL();
    glDrawElements(GL_TRIANGLES, wave_state.psp_wave.idata_sz, GL_UNSIGNED_INT, nullptr); CGL();
}

void cxl_psp_wave_draw(float ms){
    if(!HAS_FLAG(wave_state.style, WAVE_STYLE::PSP, int8_t)) return;
    psp_draw_wave();
}

void psp_destroy(){
    // Do nothing, Runtime does all
}