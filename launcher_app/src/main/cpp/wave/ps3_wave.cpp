#include "ps3_wave.h"
#include "gl.h"
#include "shaders.h"
#include "Logger.h"
#include <vector>
#include "ps3_sparkman.h"
#include "textures.h"
#include "mathutil.h"

void ps3_compile_shaders(){
    bg.shader = gltCompileShader(R::ps3_background_vert, R::ps3_background_frag);
    wave.shader = gltCompileShader(R::ps3_wave_vert, R::ps3_wave_frag);
    sparkle.shader = gltCompileShader(R::ps3_sparkle_vert, R::ps3_sparkle_frag);

    bg_unif.colorA       = glGetUniformLocation(bg     .shader, shader_unif_name.colorA);
    bg_unif.colorB       = glGetUniformLocation(bg     .shader, shader_unif_name.colorB);
    bg_unif.colorC       = glGetUniformLocation(bg     .shader, shader_unif_name.colorC);
    bg_unif.timeOfDay    = glGetUniformLocation(bg     .shader, shader_unif_name.timeOfDay);
    bg_unif.month        = glGetUniformLocation(bg     .shader, shader_unif_name.month);
    bg_attr.position     =  glGetAttribLocation(bg     .shader, shader_attr_name.position);
    bg_attr.texCoord     =  glGetAttribLocation(bg     .shader, shader_attr_name.texCoord);

    wave_unif.time       = glGetUniformLocation(wave   .shader, shader_unif_name.time);
    wave_unif.normalStep = glGetUniformLocation(wave   .shader, shader_unif_name.normalStep);
    wave_unif.ortho      = glGetUniformLocation(wave   .shader, shader_unif_name.ortho);
    wave_unif.colorA     = glGetUniformLocation(wave   .shader, shader_unif_name.colorA);
    wave_unif.colorB     = glGetUniformLocation(wave   .shader, shader_unif_name.colorB);
    wave_attr.position   =  glGetAttribLocation(wave   .shader, shader_attr_name.position);

    spark_unif.ortho     = glGetUniformLocation(sparkle.shader, shader_unif_name.ortho);
    spark_attr.position  =  glGetAttribLocation(sparkle.shader, shader_attr_name.position);
    spark_attr.texColor  =  glGetAttribLocation(sparkle.shader, shader_attr_name.vtxColor);
}

void ps3_generate_buffers(){
    GLuint tmpBuffer[6];
    glGenBuffers(6, tmpBuffer);
    bg     .vtbuf = tmpBuffer[0];
    wave   .vtbuf = tmpBuffer[1];
    sparkle.vtbuf = tmpBuffer[2];
    bg     .idbuf = tmpBuffer[3];
    wave   .idbuf = tmpBuffer[4];
    sparkle.idbuf = tmpBuffer[5];

    // fill out background buffer
    gltWriteBuffer(16, 6, bg.vtbuf, bg.idbuf, vtx_background_vdata, vtx_background_index, GL_STATIC_DRAW);

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

    gltWriteBuffer(wave_vdata_size, wave_index_size, wave.vtbuf, wave.idbuf, vtx_wave_vdata.data(), vtx_wave_index.data(), GL_STATIC_DRAW);
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
    glUseProgram(bg.shader);

    glBindBuffer(GL_ARRAY_BUFFER,         bg.vtbuf); CGL();
    glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, bg.idbuf); CGL();

    glActiveTexture(GL_TEXTURE0); CGL();
    glBindTexture(GL_TEXTURE_2D, tex_night); CGL();
    glActiveTexture(GL_TEXTURE1); CGL();
    glBindTexture(GL_TEXTURE_2D, tex_day); CGL();

    // glm::vec4 cA = background_color_top;
    // glm::vec4 cB = background_color_bottom;

    glm::vec4 cA = glm::vec4(0.0f, 1.00f, 1.00f, 1.0f);
    glm::vec4 cB = glm::vec4(0.0f, 0.50f, 1.00f, 1.0f);
    glm::vec4 cC = glm::vec4(0.0f, 0.25f, 0.50f, 1.0f);

    glUniform3f(bg_unif.colorA, cA.r, cA.g, cA.b); CGL();
    glUniform3f(bg_unif.colorB, cB.r, cB.g, cB.b); CGL();
    glUniform3f(bg_unif.colorC, cC.r, cC.g, cC.b); CGL();
    float shaderTime = timeofday_shader(timeofday());
    glUniform1f(bg_unif.timeOfDay, shaderTime);
    glUniform1i(bg_unif.month, monthofday());

    glVertexAttribPointer(bg_attr.position, 2, GL_FLOAT, GL_FALSE, 4 * sizeof(GLfloat), (GLvoid*)(0 * sizeof(GLfloat))); CGL();
    glVertexAttribPointer(bg_attr.texCoord, 2, GL_FLOAT, GL_FALSE, 4 * sizeof(GLfloat), (GLvoid*)(2 * sizeof(GLfloat))); CGL();
    glEnableVertexAttribArray(bg_attr.position); CGL();
    glEnableVertexAttribArray(bg_attr.texCoord); CGL();
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
    glUseProgram(wave.shader);
    glBindBuffer(GL_ARRAY_BUFFER, wave.vtbuf); CGL();
    glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, wave.idbuf); CGL();

    glUniform1f(wave_unif.time, currentTime * xmb_wave_speed); CGL();
    glUniform1f(wave_unif.normalStep, 0.01f); CGL();

    //glm::vec4 cA = foreground_color_edge;
    //glm::vec4 cB = foreground_color_center;

    glm::vec4 cA = glm::vec4(1.0, 1.0f, 1.0f, 0.75f);
    glm::vec4 cB = glm::vec4(1.0, 1.0f, 1.0f, 0.0f);

    glUniform4f(wave_unif.colorA, cA.r, cA.g, cA.b, cA.a); CGL();
    glUniform4f(wave_unif.colorB, cB.r, cB.g, cB.b, cB.a); CGL();

    glm::mat4 matrix = ps3_wave_matrix(); CGL();
    glUniformMatrix4fv(wave_unif.ortho, 1, GL_FALSE, &matrix[0][0]); CGL();

    glVertexAttribPointer(wave_attr.position, 3, GL_FLOAT, GL_FALSE, 3 * sizeof(GLfloat), (GLvoid*)(0 * sizeof(GLfloat))); CGL();
    glEnableVertexAttribArray(wave_attr.position); CGL();
    glDrawElements(GL_TRIANGLES, wave_index_size, GL_UNSIGNED_INT, nullptr); CGL();

}

void ps3_draw_sparkle(float ms){
    // Log_e("[PS3] Draw Sparkle");
    std::vector<GLfloat> vdata;
    std::vector<GLuint> idata;

    sparkman_fill(&vdata, &idata, ms);

    gltWriteBuffer(vdata.size(), idata.size(), sparkle.vtbuf, sparkle.idbuf, vdata.data(), idata.data(), GL_DYNAMIC_DRAW); CGL();
    glUseProgram(sparkle.shader); CGL();

    glm::mat4 matrix = ps3_wave_matrix(); CGL();
    glUniformMatrix4fv(spark_unif.ortho, 1, GL_FALSE, &matrix[0][0]); CGL();
    glBindBuffer(GL_ARRAY_BUFFER, sparkle.vtbuf);
    glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, sparkle.idbuf);
    glEnableVertexAttribArray(spark_attr.position); CGL();
    glEnableVertexAttribArray(spark_attr.texColor); CGL();
    glVertexAttribPointer(    spark_attr.position, 2, GL_FLOAT, GL_FALSE, 6 * sizeof(GLfloat), (GLvoid*)(0 * sizeof(GLfloat))); CGL();
    glVertexAttribPointer(    spark_attr.texColor, 4, GL_FLOAT, GL_FALSE, 6 * sizeof(GLfloat), (GLvoid*)(2 * sizeof(GLfloat))); CGL();

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
    glDeleteProgram(bg     .shader); CGL();
    glDeleteProgram(wave   .shader); CGL();
    glDeleteProgram(sparkle.shader); CGL();
    GLuint delBuffer[6] = { bg.vtbuf, wave.vtbuf, sparkle.vtbuf,
                            bg.idbuf, wave.idbuf, sparkle.idbuf};
    glDeleteBuffers(6, delBuffer);
}