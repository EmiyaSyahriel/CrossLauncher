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

    //wave_state.bg_mode = (AUTO_BG_MODE)((int8_t)AUTO_BG_MODE::DAY_NIGHT | (int8_t)AUTO_BG_MODE::MONTHLY);
    wave_state.vert_scale = 1.0f;

    /* XMBWaveSurfaceView will call setBackgroundMonth and setBackgroundColor
     * Every time the surface view gets out of view, GLSurfaceView will also re-run onSurfaceCreated
     * Setting these here during surface recreation will cause a preference desync
     */
    //wave_state.bg_month = MONTH_COLOR_INDEX::MONTH_CURRENT;
    //wave_state.colors.bg_a = vec3(0.0f, 1.0f, 1.0f);
    //wave_state.colors.bg_b = vec3(0.0f, 0.0f, 1.0f);
}

void cxl_bg_draw(){
    // Log_e("[PS3] Draw Background");
    glUseProgram(wave_state.bg.program);
    glBindBuffer(GL_ARRAY_BUFFER,         wave_state.bg.vt); CGL();
    glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, wave_state.bg.id); CGL();

    vec4 cA = vec4(wave_state.colors.bg_a, 1.0f);
    vec4 cB = vec4(wave_state.colors.bg_b, 1.0f);

    float shaderTime = cxl_time_of_day_shader(cxl_time_of_day());

    if(wave_state.bg_month != MONTH_CUSTOM){
        int month = ((wave_state.bg_month == MONTH_CURRENT) ?
                cxl_get_month() :
                (wave_state.bg_month - 1)) % 12;
        cA = glm::vec4(wave_consts.month_color.top[month], 1.0f);
        cB = glm::vec4(wave_consts.month_color.bottom[month], 1.0f);
    }
    if(wave_state.day_night){
        cB *= lerp(0.15f, 1.0f, shaderTime);
    }

    glUniform3f(wave_state.bg._ColorA, cA.r, cA.g, cA.b); CGL();
    glUniform3f(wave_state.bg._ColorB, cB.r, cB.g, cB.b); CGL();
    glUniform1f(wave_state.bg._TimeOfDay, shaderTime);

    glVertexAttribPointer(    wave_state.bg.POSITION, 2, GL_FLOAT, GL_FALSE, 4 * sizeof(GLfloat), (GLvoid*)(0 * sizeof(GLfloat))); CGL();
    glVertexAttribPointer(    wave_state.bg.TEXCOORD, 2, GL_FLOAT, GL_FALSE, 4 * sizeof(GLfloat), (GLvoid*)(2 * sizeof(GLfloat))); CGL();
    glEnableVertexAttribArray(wave_state.bg.POSITION); CGL();
    glEnableVertexAttribArray(wave_state.bg.TEXCOORD); CGL();

    glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_INT, nullptr); CGL();
}
