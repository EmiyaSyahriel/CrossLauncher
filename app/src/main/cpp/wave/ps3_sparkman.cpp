#include "ps3_sparkman.h"
#include <random>
#include "states.h"
#include <cmath>
#include "mathutil.h"

std::vector<Particle> particles;

const float RNG_MAX = 4096.0f;
float sparkman_delta_time = 0.016f;

std::random_device rng_dev;
std::default_random_engine engine(rng_dev());
std::uniform_real_distribution<float> rng_range(0.0f, RNG_MAX);

float rng_get(){
	return rng_range(engine) / RNG_MAX;
}

#define COLOR_RNG lerp(0.75f, 1.0f, rng_get())

void sparkman_reset(Particle &particle, bool prewarm) {
	particle.color = glm::vec3(COLOR_RNG, COLOR_RNG, COLOR_RNG);
	particle.speed = lerp(0.005f, 0.1f, rng_get());
	
	particle.pos_from = glm::vec2(snrange(rng_get()), lerp(-0.15f, 0.15f, rng_get()));

	float rot = rng_get() * (float)(M_PI * 2);
	float dis = lerp(0.05f, 0.3f, rng_get());
	particle.pos_to = particle.pos_from + (glm::vec2(cosf(rot), sinf(rot)) * dis);
	particle.size_from = lerp(0.001f, 0.005f, rng_get());
	particle.size_to = particle.size_from + lerp(0.001f, 0.005f, rng_get());
	particle.time = prewarm ? rng_get() : 0.0f;
	particle.alpha_from = lerp(0.125f, 0.25f, rng_get());
	particle.alpha_to = lerp(0.25f, 0.75f, rng_get());
}

void sparkman_move(Particle &particle) {
	particle.time += sparkman_delta_time * particle.speed;
	if(particle.time > 1.0f){
		sparkman_reset(particle, false);
	}
}

void sparkman_init()
{
	particles = std::vector<Particle>(xmb_particle_count);
	for (Particle &particle : particles) {
		sparkman_reset(particle, true);
	}
}

void sparkman_fill(std::vector<GLfloat> *vtx_data, std::vector<GLuint> *idx_data, float delta_time)
{

#ifndef PUSH_DATA
#define PUSH_DATA(pos, col, num) \
vtx_data->push_back(pos.x); vtx_data->push_back(pos.y);\
vtx_data->push_back(col.x); vtx_data->push_back(col.y);\
vtx_data->push_back(col.z); vtx_data->push_back(col.w);\
num++;
#endif

	vtx_data->clear();
	idx_data->clear();

	sparkman_delta_time = delta_time;
	const float step = 1.0f / (float)(xmb_particle_ring_count);
	int vnum = 0;

#ifndef TESTCIRCLE

	std::vector<glm::vec2> precalcs;
	for (int i = 0; i <= xmb_particle_ring_count; i++) {
		float r = ((float)i / xmb_particle_ring_count) * (M_PI *2);
		precalcs.push_back(glm::vec2(sinf(r), cosf(r)));
	}

	for (Particle &particle : particles) {
		sparkman_move(particle);

		float t = 0.0f;

		glm::vec2 pos = lerp(particle.pos_from, particle.pos_to, particle.time);
		glm::vec3 pcol = particle.color;
		float alpha = lerp(particle.alpha_from, particle.alpha_to, particle.time / 0.75f);

		if (particle.time > 0.75f) {
			alpha = lerp(particle.alpha_to, 0.0f, (particle.time - 0.75f) / 0.25f);
		}

		glm::vec4 col = glm::vec4(pcol.x, pcol.y, pcol.z, alpha);
		float size = lerp(particle.size_from, particle.size_to, particle.time);
		int svnum = vnum;

		PUSH_DATA(pos, col, vnum);

		int precount = precalcs.size();
		for (int i = 0; i < precount; i++) {
			PUSH_DATA((pos + (precalcs[i] * size)), col, vnum);

			idx_data->push_back(svnum + 0);
			idx_data->push_back(svnum + i);
			idx_data->push_back(svnum + i + 1);
		}
	}
#else

#endif

#ifdef PUSH_DATA
#undef PUSH_DATA
#endif

}
