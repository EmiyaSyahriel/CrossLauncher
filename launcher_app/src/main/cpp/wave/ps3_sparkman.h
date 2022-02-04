#ifndef H_SPARKMAN
#define H_SPARKMAN

#include <vector>
#include <glm/vec2.hpp>
#include <glm/vec3.hpp>
#include <glm/vec4.hpp>
#include "gl.h"

typedef struct {
	glm::vec2 pos_from, pos_to;
	float size_from, size_to;
	float alpha_from, alpha_to;
	float time, speed;
	glm::vec3 color;
} Particle;

extern std::vector<Particle> particles;

void sparkman_init();

void sparkman_fill(std::vector<GLfloat> *vtx_data, std::vector<GLuint> *idx_data, float delta_time);

#endif