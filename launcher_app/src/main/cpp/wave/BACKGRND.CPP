/**
 * Created by EmiyaSyahriel, 05:36 AM 15/04/2022
 * Background Object Rendering
 */
#include "WAVE.HPP"

void cxl_bg_start(){
    cxl_gl_write_buffer(16, 6, wave_state.bg.vt, wave_state.bg.id, wave_consts.bg_mdl_v, wave_consts.bg_mdl_i, GL_STATIC_DRAW);
    wave_state.bg._ColorA    = glGetUniformLocation(wave_state.bg.program, wave_consts.unif_names._ColorA   );
    wave_state.bg._ColorB    = glGetUniformLocation(wave_state.bg.program, wave_consts.unif_names._ColorB   );
    wave_state.bg._TimeOfDay = glGetUniformLocation(wave_state.bg.program, wave_consts.unif_names._TimeOfDay);
    wave_state.bg.POSITION =   glGetAttribLocation (wave_state.bg.program, wave_consts.attr_names.POSITION);
    wave_state.bg.TEXCOORD =   glGetAttribLocation (wave_state.bg.program, wave_consts.attr_names.TEXCOORD0);
}

void cxl_bg_draw(){
    // Log_e("[PS3] Draw Background");
    glUseProgram(wave_state.bg.program);
    glBindBuffer(GL_ARRAY_BUFFER,         wave_state.bg.vt); CGL();
    glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, wave_state.bg.id); CGL();

    vec4 cA = vec4(0.0f, 1.00f, 1.00f, 1.0f);
    vec4 cB = vec4(0.0f, 0.50f, 1.00f, 1.0f);

    float shaderTime = cxl_time_of_day_shader(cxl_time_of_day());
    glUniform3f(wave_state.bg._ColorA   , cA.r, cA.g, cA.b); CGL();
    glUniform3f(wave_state.bg._ColorB   , cB.r, cB.g, cB.b); CGL();
    glUniform1f(wave_state.bg._TimeOfDay, shaderTime);

    glVertexAttribPointer(    wave_state.bg.POSITION, 2, GL_FLOAT, GL_FALSE, 4 * sizeof(GLfloat), (GLvoid*)(0 * sizeof(GLfloat))); CGL();
    glVertexAttribPointer(    wave_state.bg.TEXCOORD, 2, GL_FLOAT, GL_FALSE, 4 * sizeof(GLfloat), (GLvoid*)(2 * sizeof(GLfloat))); CGL();
    glEnableVertexAttribArray(wave_state.bg.POSITION); CGL();
    glEnableVertexAttribArray(wave_state.bg.TEXCOORD); CGL();

    glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_INT, nullptr); CGL();
}
