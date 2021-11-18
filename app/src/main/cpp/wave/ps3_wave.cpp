#include "ps3_wave.h"
#include "gl.h"
#include "shaders.h"
#include "Logger.h"
#include <vector>
#include "ps3_sparkman.h"

GLuint shader_bg, shader_wave, shader_sparkle;
GLuint vtbuff_bg, vtbuff_wave, vtbuff_sparkle;
GLuint idbuff_bg, idbuff_wave, idbuff_sparkle;

GLint vunif_bg_ColorA, vunif_bg_ColorB, vattr_bg_vpos, vattr_bg_tpos;

GLint vunif_wave_Time, vunif_wave_ScreenSize, vunif_wave_RefSize, vunif_wave_NormalStep, vattr_wave_vpos;
GLint vunif_wave_white, vunif_wave_color, vunif_wave_Ortho;

GLint vattr_spark_vpos, vattr_spark_vcol;
GLint vunif_spark_matrix;

float currentTime = 0.0f;
int refWidth = 1280, refHeight = 720, screenWidth = 1280, screenHeight= 720;

void ps3_compile_shaders(){
    shader_bg = gltCompileShader(R::ps3_background_vert, R::ps3_background_frag);
    shader_wave = gltCompileShader(R::ps3_wave_vert, R::ps3_wave_frag);
    shader_sparkle = gltCompileShader(R::ps3_sparkle_vert, R::ps3_sparkle_frag); // TODO: Create real sparkle shader

    vunif_bg_ColorA = glGetUniformLocation(shader_bg, "_ColorA");
    vunif_bg_ColorB = glGetUniformLocation(shader_bg, "_ColorB");
    vattr_bg_vpos = glGetAttribLocation(shader_bg, "vpos");
    vattr_bg_tpos = glGetAttribLocation(shader_bg, "uv");

    vunif_wave_Time = glGetUniformLocation(shader_wave, "_Time");
    vunif_wave_ScreenSize = glGetUniformLocation(shader_wave, "_ScreenSize");
    vunif_wave_RefSize = glGetUniformLocation(shader_wave, "_RefSize");
    vunif_wave_NormalStep = glGetUniformLocation(shader_wave, "_NormalStep");
    vunif_wave_Ortho = glGetUniformLocation(shader_wave, "_Ortho");
    vunif_wave_white = glGetUniformLocation(shader_wave, "white");
    vunif_wave_color = glGetUniformLocation(shader_wave, "color");
    vattr_wave_vpos = glGetAttribLocation(shader_wave, "position");

    vunif_spark_matrix = glGetUniformLocation(shader_sparkle, "matrix");
    vattr_spark_vpos = glGetAttribLocation(shader_sparkle, "vpos");
    vattr_spark_vcol = glGetAttribLocation(shader_sparkle, "vcol");
}

GLfloat vtx_background_vdata[16] = {
         -1.0f,  -1.0f, -1.0f, -1.0f, // TL
         1.0f,  -1.0f,  1.0f, -1.0f, // TR
         -1.0f,  1.0f, -1.0f,  1.0f, // BL
         1.0f,  1.0f,  1.0f,  1.0f, // BR
};

GLuint vtx_background_index[6] = { 0,1,2,1,3,2 };

int wave_index_size, wave_vdata_size;

void ps3_generate_buffers(){
    glGenBuffers(1, &vtbuff_bg);
    glGenBuffers(1, &vtbuff_wave);
    glGenBuffers(1, &vtbuff_sparkle);
    glGenBuffers(1, &idbuff_bg);
    glGenBuffers(1, &idbuff_wave);
    glGenBuffers(1, &idbuff_sparkle);

    // fill out background buffer
    gltWriteBuffer(16, 6, vtbuff_bg, idbuff_bg, vtx_background_vdata, vtx_background_index, GL_STATIC_DRAW);

    // fill wave buffer
    std::vector<GLfloat> vtx_wave_vdata;
    std::vector<GLuint> vtx_wave_index;

    int vx, vy;

#define POS_NORMALIZE(t) (((float)t / (float)(xmb_detail_size - 1)) * 2) - 1

    for (vy = 0; vy < xmb_detail_size; vy ++) {
        for (vx = 0; vx < xmb_detail_size; vx ++) {
            vtx_wave_vdata.push_back(POS_NORMALIZE(vx));
            vtx_wave_vdata.push_back(POS_NORMALIZE(vy));
            vtx_wave_vdata.push_back(0.0f);
        }
    }
#undef POS_NORMALIZE

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

    gltWriteBuffer(wave_vdata_size, wave_index_size, vtbuff_wave, idbuff_wave, vtx_wave_vdata.data(), vtx_wave_index.data(), GL_DYNAMIC_DRAW);
}

void ps3_resize(float w, float h){
    glViewport((GLint)0.0f,(GLint)0.0f, (GLsizei)w, (GLsizei)h);
    screenWidth = (int)w;
    screenHeight = (int)h;
}

void ps3_start(){
    glClearColor(0.6f, 0.0f, 1.0f, 1.0f);
    ps3_compile_shaders();
    ps3_generate_buffers();


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
    glUseProgram(shader_bg);

    glBindBuffer(GL_ARRAY_BUFFER, vtbuff_bg); CGL();
    glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, idbuff_bg); CGL();

    glm::vec4 cA = background_color_top;
    glm::vec4 cB = background_color_bottom;

    glUniform3f(vunif_bg_ColorA, cA.r, cA.g, cA.b); CGL();
    glUniform3f(vunif_bg_ColorB, cB.r, cB.g, cB.b); CGL();

    glVertexAttribPointer(vattr_bg_vpos, 2, GL_FLOAT, GL_FALSE, 4 * sizeof(GLfloat), (GLvoid*)(0 * sizeof(GLfloat))); CGL();
    glVertexAttribPointer(vattr_bg_tpos, 2, GL_FLOAT, GL_FALSE, 4 * sizeof(GLfloat), (GLvoid*)(2 * sizeof(GLfloat))); CGL();
    glEnableVertexAttribArray(vattr_bg_vpos); CGL();
    glEnableVertexAttribArray(vattr_bg_tpos); CGL();
    glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_INT, nullptr); CGL();
}

glm::mat4 ps3_wave_matrix() {
    float scaleX = (float)screenWidth / screenHeight;
    float scaleY = (float)screenHeight / screenWidth;
    if (scaleX > scaleY) {
        return glm::ortho(-1.0f, 1.0f, -scaleY, scaleY, -10.0f, 10.0f);
    }
    else {
        return glm::ortho(-scaleX, scaleX, -1.0f, 1.0f, -10.0f, 10.0f);
    }
}

void ps3_draw_wave(){
    glUseProgram(shader_wave);
    glBindBuffer(GL_ARRAY_BUFFER, vtbuff_wave); CGL();
    glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, idbuff_wave); CGL();

    glUniform1f(vunif_wave_Time, currentTime * xmb_wave_speed); CGL();
    glUniform1f(vunif_wave_NormalStep, 0.01f); CGL();
    glUniform2f(vunif_wave_ScreenSize, screenWidth, screenHeight); CGL();
    glUniform2f(vunif_wave_RefSize, refWidth, refHeight); CGL();

    glm::vec4 cA = foreground_color_edge;
    glm::vec4 cB = foreground_color_center;

    glUniform4f(vunif_wave_white, cA.r, cA.g, cA.b, cA.a); CGL();
    glUniform4f(vunif_wave_color, cB.r, cB.g, cB.b, cB.a); CGL();

    glm::mat4 matrix = ps3_wave_matrix(); CGL();
    glUniformMatrix4fv(vunif_wave_Ortho, 1, GL_FALSE, &matrix[0][0]); CGL();

    glVertexAttribPointer(vattr_wave_vpos, 3, GL_FLOAT, GL_FALSE, 3 * sizeof(GLfloat), (GLvoid*)(0 * sizeof(GLfloat))); CGL();
    glEnableVertexAttribArray(vattr_wave_vpos); CGL();
    glDrawElements(GL_TRIANGLES, wave_index_size, GL_UNSIGNED_INT, nullptr); CGL();

}

void ps3_draw_sparkle(float ms){
    std::vector<GLfloat> vdata;
    std::vector<GLuint> idata;

    sparkman_fill(&vdata, &idata, ms);

    gltWriteBuffer(vdata.size(), idata.size(), vtbuff_sparkle, idbuff_sparkle, vdata.data(), idata.data(), GL_DYNAMIC_DRAW); CGL();
    glUseProgram(shader_sparkle); CGL();

    glm::mat4 matrix = ps3_wave_matrix(); CGL();
    glUniformMatrix4fv(vunif_spark_matrix, 1, GL_FALSE, &matrix[0][0]); CGL();
    glBindBuffer(GL_ARRAY_BUFFER, vtbuff_sparkle);
    glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, idbuff_sparkle);
    glEnableVertexAttribArray(vattr_spark_vpos); CGL();
    glEnableVertexAttribArray(vattr_spark_vcol); CGL();
    glVertexAttribPointer(vattr_spark_vpos, 2, GL_FLOAT, GL_FALSE, 6 * sizeof(GLfloat), (GLvoid*)(0 * sizeof(GLfloat))); CGL();
    glVertexAttribPointer(vattr_spark_vcol, 4, GL_FLOAT, GL_FALSE, 6 * sizeof(GLfloat), (GLvoid*)(2 * sizeof(GLfloat))); CGL();

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
    glDeleteBuffers(1, &vtbuff_bg);
    glDeleteBuffers(1, &vtbuff_wave);
    glDeleteBuffers(1, &vtbuff_sparkle);
    glDeleteBuffers(1, &idbuff_bg);
    glDeleteBuffers(1, &idbuff_wave);
    glDeleteBuffers(1, &idbuff_sparkle);
}