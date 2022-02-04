#include "textures.h"
#include <vector>
#include "gl.h"

/**
Day shade colors
*/
const char siang_shades[] =
"3321""2333""1122""1111""1122""1111""2332""2333""3322""3322""2333""1112"
"2122""2112""1222""1112""1112""1122""2222""1223""3221""2321""2233""1122"
"1112""1111""2233""1122""1112""2222""1122""1122""2221""2111""2111""1233"
"2123""2112""2221""1222""1122""2332""1112""1112""2211""1111""1111""1233";

/**
Night shade colors
*/
const char malam_shades[] = 
"0001""1110""0010""0001""0110""0010""1100""1100""1000""1100""0001""0001"
"1111""1122""1121""1112""1111""1111""1111""2111""2111""1111""1112""0011"
"1111""1222""2222""2222""1122""1212""1122""2211""2221""1111""1221""1112"
"2222""2222""2211""2222""2222""2222""2222""2222""2222""2222""2222""1222";

/**
Load texture 4bit (0-3 ASCII) to OpenGL
with mapping at
*/
void load_texture(const char* which, GLuint* to, int size, int width, int height)
{
	glGenTextures(1, to);

	std::vector<float> texture_data;
	for (int i = 0;i < size; i++) {
		char ch = which[i];
		if (ch >= '0' && ch <= '3') {
			float f = (float)((ch - '0')) * 0.25f;
			texture_data.push_back(f);
		}
	}
	glBindTexture(GL_TEXTURE_2D, to[0]);
	glTexImage2D(GL_TEXTURE_2D, 0, GL_LUMINANCE, width, height, 0, GL_LUMINANCE, GL_FLOAT, texture_data.data());
	glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_MIRRORED_REPEAT);
	glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_MIRRORED_REPEAT);
	glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
	glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
}
