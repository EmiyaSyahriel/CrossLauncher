/**
 * Created by EmiyaSyahriel on 12/05/2021.
 * PlayStation 3 Wave Renderer
 *
 * The Wave itself is procedurally calculated in the vertex shader, the generator codes actually
 * only generates vertices once to be used by the shader on every draw call.
 */
#include "WAVE.HPP"
#include <vector>

std::vector<GLfloat> vtx_wave_vdata;
std::vector<GLuint> vtx_wave_index;

void cxl_ps3_wave_query_shader_interfaces(){
    wave_state.ps3_wave._Time       = glGetUniformLocation(wave_state.ps3_wave.program, wave_consts.unif_names._Time);
    wave_state.ps3_wave._NormalStep = glGetUniformLocation(wave_state.ps3_wave.program, wave_consts.unif_names._NormalStep);
    wave_state.ps3_wave._Ortho      = glGetUniformLocation(wave_state.ps3_wave.program, wave_consts.unif_names._Ortho);
    wave_state.ps3_wave._ColorA     = glGetUniformLocation(wave_state.ps3_wave.program, wave_consts.unif_names._ColorA);
    wave_state.ps3_wave._ColorB     = glGetUniformLocation(wave_state.ps3_wave.program, wave_consts.unif_names._ColorB);
    wave_state.ps3_wave._YScale     = glGetUniformLocation(wave_state.ps3_wave.program, wave_consts.unif_names._YScale);
    wave_state.ps3_wave._RngDataA = glGetUniformLocation(wave_state.ps3_wave.program, wave_consts.unif_names._RngDataA);
    wave_state.ps3_wave._RngDataB = glGetUniformLocation(wave_state.ps3_wave.program, wave_consts.unif_names._RngDataB);
    wave_state.ps3_wave._RngTrans = glGetUniformLocation(wave_state.ps3_wave.program, wave_consts.unif_names._RngTrans);
    wave_state.ps3_wave.POSITION   =  glGetAttribLocation(wave_state.ps3_wave.program, wave_consts.attr_names.POSITION);
}

#define POS_NORMALIZE(i) (snrange(((i) * 1.1f) / (float)wave_consts.detail_size))

void cxl_ps3_wave_generate_vertex(){
    // fill out background buffer
    // fill wave buffer
    vtx_wave_vdata.clear();
    vtx_wave_index.clear();

    int vx, vy;

    for (vy = 0; vy < wave_consts.detail_size; vy ++) {
        for (vx = 0; vx < wave_consts.detail_size; vx ++) {
            vtx_wave_vdata.push_back(POS_NORMALIZE(vx));
            vtx_wave_vdata.push_back(POS_NORMALIZE(vy));
            vtx_wave_vdata.push_back(0.0f);
        }
    }

    int ix, iy;
    for (iy = 0; iy < wave_consts.detail_size - 1; iy++) {
        for (ix = 0; ix < wave_consts.detail_size - 1; ix++) {
            int top = (iy * wave_consts.detail_size) + ix;
            int btm = ((iy+1) * wave_consts.detail_size) + ix;
            int a = top + 0, b = top + 1, c = btm + 0, d = btm + 1;
            vtx_wave_index.push_back(a);
            vtx_wave_index.push_back(b);
            vtx_wave_index.push_back(c);
            vtx_wave_index.push_back(b);
            vtx_wave_index.push_back(d);
            vtx_wave_index.push_back(c);
        }
    }

    wave_state.ps3_wave.vdata_sz = vtx_wave_vdata.size();
    wave_state.ps3_wave.idata_sz = vtx_wave_index.size();

    cxl_gl_write_buffer(wave_state.ps3_wave.vdata_sz, wave_state.ps3_wave.idata_sz, wave_state.ps3_wave.vt, wave_state.ps3_wave.id, vtx_wave_vdata.data(), vtx_wave_index.data(), GL_STATIC_DRAW);
}

void cxl_ps3_wave_start(){
    cxl_ps3_wave_query_shader_interfaces();
    cxl_ps3_wave_generate_vertex();
}

void cxl_ps3_wave_draw(float ms){
    // Log_e("[PS3] Draw Wave");
    glUseProgram(wave_state.ps3_wave.program);
    glBindBuffer(GL_ARRAY_BUFFER, wave_state.ps3_wave.vt); CGL();
    glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, wave_state.ps3_wave.id); CGL();

    glUniform1f(wave_state.ps3_wave._Time, wave_state.current_time); CGL();
    glUniform1f(wave_state.ps3_wave._NormalStep, 0.01f); CGL();

    glm::vec4 cA = wave_state.colors.fg_a;
    glm::vec4 cB = wave_state.colors.fg_b;

    glUniform4f(wave_state.ps3_wave._ColorA, cA.r, cA.g, cA.b, cA.a); CGL();
    glUniform4f(wave_state.ps3_wave._ColorB, cB.r, cB.g, cB.b, cB.a); CGL();

    glUniform1fv(wave_state.ps3_wave._RngDataA, 16, wave_state.rng_data.A); CGL();
    glUniform1fv(wave_state.ps3_wave._RngDataB, 16, wave_state.rng_data.B); CGL();
    glUniform1f (wave_state.ps3_wave._RngTrans, wave_state.rng_data.t); CGL();
    glUniform1f (wave_state.ps3_wave._YScale, wave_state.vert_scale); CGL();

    glm::mat4 matrix = cxl_wave_matrix(); CGL();
    glUniformMatrix4fv(wave_state.ps3_wave._Ortho, 1, GL_FALSE, &matrix[0][0]); CGL();

    glVertexAttribPointer(    wave_state.ps3_wave.POSITION, 3, GL_FLOAT, GL_FALSE, 3 * sizeof(GLfloat), (GLvoid*)(0 * sizeof(GLfloat))); CGL();
    glEnableVertexAttribArray(wave_state.ps3_wave.POSITION); CGL();
    glDrawElements(GL_TRIANGLES, wave_state.ps3_wave.idata_sz, GL_UNSIGNED_INT, nullptr); CGL();
}