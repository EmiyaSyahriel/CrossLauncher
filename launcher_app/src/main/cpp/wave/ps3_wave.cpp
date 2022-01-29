#include "ps3_wave.h"
#include "gl.h"
#include "shaders.h"
#include "Logger.h"
#include <vector>
#include "ps3_sparkman.h"
#include "textures.h"
#include "mathutil.h"

void ps3_compile_shaders(){
    shader_bg = gltCompileShader(R::ps3_background_vert, R::ps3_background_frag);
    shader_wave = gltCompileShader(R::ps3_wave_vert, R::ps3_wave_frag);
    shader_sparkle = gltCompileShader(R::ps3_sparkle_vert, R::ps3_sparkle_frag); 

    vunif_bg_ColorA       = glGetUniformLocation(shader_bg, shuColorA);
    vunif_bg_ColorB       = glGetUniformLocation(shader_bg, shuColorB);
    vunif_bg_ColorC       = glGetUniformLocation(shader_bg, shuColorC);
    vunif_bg_TimeOfDay    = glGetUniformLocation(shader_bg, shuTimeOfDay);
    vunif_bg_Month        = glGetUniformLocation(shader_bg, shuMonth);
    vattr_bg_Position     = glGetAttribLocation(shader_bg, shaPosition);
    vattr_bg_TexCoord     = glGetAttribLocation(shader_bg, shaTexCoord);

    vunif_wave_Time       = glGetUniformLocation(shader_wave, shuTime);
    vunif_wave_NormalStep = glGetUniformLocation(shader_wave, shuNormalStep);
    vunif_wave_Ortho      = glGetUniformLocation(shader_wave, shuOrtho);
    vunif_wave_ColorA     = glGetUniformLocation(shader_wave, shuColorA);
    vunif_wave_ColorB     = glGetUniformLocation(shader_wave, shuColorB);
    vattr_wave_Position   = glGetAttribLocation(shader_wave, shaPosition);

    vunif_spark_Ortho    = glGetUniformLocation(shader_sparkle, shuOrtho);
    vattr_spark_Position = glGetAttribLocation(shader_sparkle, shaPosition);
    vattr_spark_TexColor = glGetAttribLocation(shader_sparkle, shaVtxColor);
}

void ps3_generate_buffers(){
    GLuint tmpBuffer[6];
    glGenBuffers(6, tmpBuffer);
    vtbuff_bg      = tmpBuffer[0];
    vtbuff_wave    = tmpBuffer[1];
    vtbuff_sparkle = tmpBuffer[2];
    idbuff_bg      = tmpBuffer[3];
    idbuff_wave    = tmpBuffer[4];
    idbuff_sparkle = tmpBuffer[5];

    // fill out background buffer
    gltWriteBuffer(16, 6, vtbuff_bg, idbuff_bg, vtx_background_vdata, vtx_background_index, GL_STATIC_DRAW);

    // fill wave buffer
    std::vector<GLfloat> vtx_wave_vdata;
    std::vector<GLuint> vtx_wave_index;

    int vx, vy;

    for (vy = 0; vy < xmb_detail_size; vy ++) {
        for (vx = 0; vx < xmb_detail_size; vx ++) {
            vtx_wave_vdata.push_back(POS_NORMALIZE(vx));
            vtx_wave_vdata.push_back(POS_NORMALIZE(vy));
            vtx_wave_vdata.push_back(0.0f);
        }
    }

    int ix, iy;
    for (iy = 0; iy < xmb_detail_size - 1; iy++) {
        for (ix = 0; ix < xmb_detail_size - 1; ix++) {
            int top = (iy * xmb_detail_size) + ix;
            int btm = ((iy+1) * xmb_detail_size) + ix;
            int a = top + 0, b = top + 1, c = btm + 0, d = btm + 1;
            vtx_wave_index.push_back(a);
            vtx_wave_index.push_back(b);
            vtx_wave_index.push_back(c);
            vtx_wave_index.push_back(b);
            vtx_wave_index.push_back(d);
            vtx_wave_index.push_back(c);
        }
    }

    wave_vdata_size = vtx_wave_vdata.size();
    wave_index_size = vtx_wave_index.size();

    gltWriteBuffer(wave_vdata_size, wave_index_size, vtbuff_wave, idbuff_wave, vtx_wave_vdata.data(), vtx_wave_index.data(), GL_STATIC_DRAW);
}

void ps3_resize(float w, float h){
    glViewport((GLint)0.0f,(GLint)0.0f, (GLsizei)w, (GLsizei)h);
    xmb_screen_w = (int)w;
    xmb_screen_h = (int)h;
}

void ps3_start(){
    glClearColor(0.6f, 0.0f, 1.0f, 1.0f);
    ps3_compile_shaders();
    ps3_generate_buffers();
    load_texture(siang_shades, &tex_day);
    load_texture(malam_shades, &tex_night);

    if (wave_type == WAVE_TYPE::PS3_BLINKS) {
        sparkman_init();
        Log_d("Profile: PS3 SPARKLE");
    }else{
        Log_d("Profile: PS3 CLASSIC");
    }
}

void ps3_wave_setup_blend(){
    glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
    glEnable(GL_BLEND);
    glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
}

void ps3_wave_unsetup_blend(){
    gltResetBoundBuffer();
    glBlendFunc(GL_ONE, GL_ZERO);
    glDisable(GL_BLEND);
    glUseProgram(0);
}

void ps3_draw_background(){
    // Log_e("[PS3] Draw Background");
    glUseProgram(shader_bg);

    glBindBuffer(GL_ARRAY_BUFFER, vtbuff_bg); CGL();
    glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, idbuff_bg); CGL();

    glActiveTexture(GL_TEXTURE0); CGL();
    glBindTexture(GL_TEXTURE_2D, tex_night); CGL();
    glActiveTexture(GL_TEXTURE1); CGL();
    glBindTexture(GL_TEXTURE_2D, tex_day); CGL();

    // glm::vec4 cA = background_color_top;
    // glm::vec4 cB = background_color_bottom;

    glm::vec4 cA = glm::vec4(0.0f, 1.00f, 1.00f, 1.0f);
    glm::vec4 cB = glm::vec4(0.0f, 0.50f, 1.00f, 1.0f);
    glm::vec4 cC = glm::vec4(0.0f, 0.25f, 0.50f, 1.0f);

    glUniform3f(vunif_bg_ColorA, cA.r, cA.g, cA.b); CGL();
    glUniform3f(vunif_bg_ColorB, cB.r, cB.g, cB.b); CGL();
    glUniform3f(vunif_bg_ColorC, cC.r, cC.g, cC.b); CGL();
    float shaderTime = timeofday_shader(timeofday());
    glUniform1f(vunif_bg_TimeOfDay, shaderTime);
    glUniform1i(vunif_bg_Month, monthofday());

    glVertexAttribPointer(vattr_bg_Position, 2, GL_FLOAT, GL_FALSE, 4 * sizeof(GLfloat), (GLvoid*)(0 * sizeof(GLfloat))); CGL();
    glVertexAttribPointer(vattr_bg_TexCoord, 2, GL_FLOAT, GL_FALSE, 4 * sizeof(GLfloat), (GLvoid*)(2 * sizeof(GLfloat))); CGL();
    glEnableVertexAttribArray(vattr_bg_Position); CGL();
    glEnableVertexAttribArray(vattr_bg_TexCoord); CGL();
    glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_INT, nullptr); CGL();
}

glm::mat4 ps3_wave_matrix() {
    float scaleX = (float)xmb_screen_w / xmb_screen_h;
    float scaleY = (float)xmb_screen_h / xmb_screen_w;
    if (scaleX > scaleY) {
        return glm::ortho(-1.0f, 1.0f, -scaleY, scaleY, -10.0f, 10.0f);
    }
    else {
        return glm::ortho(-scaleX, scaleX, -1.0f, 1.0f, -10.0f, 10.0f);
    }
}

void ps3_draw_wave(){
    // Log_e("[PS3] Draw Wave");
    glUseProgram(shader_wave);
    glBindBuffer(GL_ARRAY_BUFFER, vtbuff_wave); CGL();
    glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, idbuff_wave); CGL();

    glUniform1f(vunif_wave_Time, currentTime * xmb_wave_speed); CGL();
    glUniform1f(vunif_wave_NormalStep, 0.01f); CGL();

    //glm::vec4 cA = foreground_color_edge;
    //glm::vec4 cB = foreground_color_center;

    glm::vec4 cA = glm::vec4(1.0, 1.0f, 1.0f, 0.75f);
    glm::vec4 cB = glm::vec4(1.0, 1.0f, 1.0f, 0.0f);

    glUniform4f(vunif_wave_ColorA, cA.r, cA.g, cA.b, cA.a); CGL();
    glUniform4f(vunif_wave_ColorB, cB.r, cB.g, cB.b, cB.a); CGL();

    glm::mat4 matrix = ps3_wave_matrix(); CGL();
    glUniformMatrix4fv(vunif_wave_Ortho, 1, GL_FALSE, &matrix[0][0]); CGL();

    glVertexAttribPointer(vattr_wave_Position, 3, GL_FLOAT, GL_FALSE, 3 * sizeof(GLfloat), (GLvoid*)(0 * sizeof(GLfloat))); CGL();
    glEnableVertexAttribArray(vattr_wave_Position); CGL();
    glDrawElements(GL_TRIANGLES, wave_index_size, GL_UNSIGNED_INT, nullptr); CGL();

}

void ps3_draw_sparkle(float ms){
    // Log_e("[PS3] Draw Sparkle");
    std::vector<GLfloat> vdata;
    std::vector<GLuint> idata;

    sparkman_fill(&vdata, &idata, ms);

    gltWriteBuffer(vdata.size(), idata.size(), vtbuff_sparkle, idbuff_sparkle, vdata.data(), idata.data(), GL_DYNAMIC_DRAW); CGL();
    glUseProgram(shader_sparkle); CGL();

    glm::mat4 matrix = ps3_wave_matrix(); CGL();
    glUniformMatrix4fv(vunif_spark_Ortho, 1, GL_FALSE, &matrix[0][0]); CGL();
    glBindBuffer(GL_ARRAY_BUFFER, vtbuff_sparkle);
    glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, idbuff_sparkle);
    glEnableVertexAttribArray(vattr_spark_Position); CGL();
    glEnableVertexAttribArray(vattr_spark_TexColor); CGL();
    glVertexAttribPointer(vattr_spark_Position, 2, GL_FLOAT, GL_FALSE, 6 * sizeof(GLfloat), (GLvoid*)(0 * sizeof(GLfloat))); CGL();
    glVertexAttribPointer(vattr_spark_TexColor, 4, GL_FLOAT, GL_FALSE, 6 * sizeof(GLfloat), (GLvoid*)(2 * sizeof(GLfloat))); CGL();

    glDrawElements(GL_TRIANGLES, idata.size(), GL_UNSIGNED_INT, nullptr); CGL();
}

void ps3_draw(float ms){
    // return prematurely in-case it doesn't has flag for PS3
    if(!HAS_FLAG(wave_type, WAVE_TYPE::PS3, int8_t)) return;

    currentTime += ms;
    if (currentTime >= 100000.0f)  currentTime = 0.0f;

    ps3_wave_setup_blend();

    ps3_draw_background();
    ps3_draw_wave();

    if(wave_type == WAVE_TYPE::PS3_BLINKS){
        ps3_draw_sparkle(ms);
    }

    ps3_wave_unsetup_blend();
}

void ps3_destroy(){
    glDeleteProgram(shader_bg); CGL();
    glDeleteProgram(shader_wave); CGL();
    glDeleteProgram(shader_sparkle); CGL();
    GLuint delBuffer[6] = { vtbuff_bg, vtbuff_wave, vtbuff_sparkle,
                            idbuff_bg, idbuff_wave, idbuff_sparkle};
    glDeleteBuffers(6, delBuffer);
}