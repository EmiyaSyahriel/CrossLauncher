#include "textures.h"
#include <vector>
#include "gl.h"

/**
Day shade colors
*/
const char siang_shades[] =
"2221""2223""1122""1111"
"2122""2112""1222""1112"
"1112""1111""2222""1122"
"2122""2112""2221""1222"
//----------------
"1122""1111""2222""2222"
"1112""1122""2222""1222"
"1112""2222""1122""1122"
"1122""2222""1112""1112"
//----------------
"2222""2222""2222""1112"
"2221""2221""2222""1122"
"2221""2111""2111""1222"
"2211""1111""1111""1222";

/**
Night shade colors
*/
const char malam_shades[] = 
		"0000""0000""0000""0000"
		"1111""1122""1121""1112"
		"1111""1222""2222""2222"
		"2222""2222""2211""2222"

		"0000""0000""0000""1100"
		"1111""1111""1111""2111"
		"1122""1212""1122""2211"
		"2222""2222""2222""2222"

		"1000""0000""0000""0000"
		"2111""1111""1112""0011"
		"2221""1111""1221""1112"
		"2222""2222""2222""1222";

/**
Load texture 4bit (0-3 ASCII) to OpenGL
with mapping at
*/
void load_texture(const char* which, GLuint* to, int size, int width, int height)
{
	glGenTextures(1, to);

	std::vector<char> texture_data;
	std::vector<char> texture_chars(which, which + size);
	for (char ch : texture_chars) {
		if (ch >= '0' && ch <= '3') {
			char f = ((char)(ch - '0')) * (255/4);
			texture_data.push_back(f);
		}
	}
	glBindTexture(GL_TEXTURE_2D, to[0]);
	glTexImage2D(GL_TEXTURE_2D, 0, GL_LUMINANCE, width, height, 0, GL_LUMINANCE, GL_UNSIGNED_BYTE, texture_data.data());
	glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_MIRRORED_REPEAT);
	glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_MIRRORED_REPEAT); 
	glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
	glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
}
