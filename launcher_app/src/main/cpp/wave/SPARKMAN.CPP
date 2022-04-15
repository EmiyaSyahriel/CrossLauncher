/**
 * Created by EmiyaSyahriel on 18/11/2021.
 * Sparkle Particle System and Renderer
 */
#include "WAVE.HPP"
#include <cmath>

std::vector<particle_t> particles;

const float RNG_MAX = 4096.0f;
float sparkman_delta_time = 0.016f;

#define COLOR_RNG lerp(0.75f, 1.0f, cxl_get_rng())
#define FLOAT_RNG cxl_get_rng()
#define WEIGHTING(x) powf(fabs(cosf((1.0f - (x)) * M_PI)), 1.0f)

void sparkman_reset(particle_t &particle, bool prewarm) {
	particle.color = glm::vec3(COLOR_RNG, COLOR_RNG, COLOR_RNG);
	particle.speed = lerp(0.005f, 0.1f, cxl_get_rng());
	
	float from_x = WEIGHTING(FLOAT_RNG);
	float y_range = 0.15f * WEIGHTING(from_x);
	float from_y = lerp(-y_range, y_range, FLOAT_RNG);// *;

	particle.pos_from = glm::vec2(snrange(from_x), from_y);

	float rot = FLOAT_RNG * (float)(M_PI * 2);
	float dis = lerp(0.05f, 0.3f, FLOAT_RNG * WEIGHTING(from_x));
	particle.pos_to = particle.pos_from + (glm::vec2(cosf(rot), sinf(rot)) * dis);
	particle.size_from = lerp(0.001f, 0.005f, FLOAT_RNG);
	particle.size_to = particle.size_from + lerp(0.001f, 0.005f, FLOAT_RNG);
	particle.time = prewarm ? FLOAT_RNG : 0.0f;
	particle.alpha_from = lerp(0.125f, 0.25f, FLOAT_RNG);
	particle.alpha_to = lerp(0.25f, 0.75f, FLOAT_RNG);
}

void sparkman_move(particle_t &particle) {
	particle.time += sparkman_delta_time * particle.speed;
	if(particle.time > 1.0f){
		sparkman_reset(particle, false);
	}
}

void sparkman_init()
{
	particles = std::vector<particle_t>(wave_consts.particle_count);
	for (particle_t &particle : particles) {
		sparkman_reset(particle, true);
	}

	wave_state.spark.POSITION = glGetAttribLocation(wave_state.spark.program, wave_consts.attr_names.POSITION);
	wave_state.spark.COLOR = glGetAttribLocation(wave_state.spark.program, wave_consts.attr_names.COLOR);

	wave_state.spark._Ortho = glGetUniformLocation(wave_state.spark.program, wave_consts.unif_names._Ortho);
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
	const float step = 1.0f / (float)(wave_consts.ring_count);
	int vnum = 0;

#ifndef TESTCIRCLE

	std::vector<glm::vec2> precalcs;
	for (int i = 0; i <= wave_consts.ring_count; i++) {
		float r = ((float)i / wave_consts.ring_count) * ((float)M_PI *2);
		precalcs.push_back(glm::vec2(sinf(r), cosf(r)));
	}

	for (particle_t &particle : particles) {
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

void cxl_spark_start() {
	sparkman_init();
}

void cxl_spark_draw(float ms) {
	// Log_e("[PS3] Draw Sparkle");
	std::vector<GLfloat> vdata;
	std::vector<GLuint> idata;

	sparkman_fill(&vdata, &idata, ms);

	cxl_gl_write_buffer(vdata.size(), idata.size(), wave_state.spark.vt, wave_state.spark.id, vdata.data(), idata.data(), GL_DYNAMIC_DRAW); CGL();
	glUseProgram(wave_state.spark.program); CGL();

	glm::mat4 matrix = cxl_wave_matrix(); CGL();
	glUniformMatrix4fv(wave_state.spark._Ortho, 1, GL_FALSE, &matrix[0][0]); CGL();
	glBindBuffer(GL_ARRAY_BUFFER, wave_state.spark.vt);
	glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, wave_state.spark.id);
	glEnableVertexAttribArray(wave_state.spark.POSITION); CGL();
	glEnableVertexAttribArray(wave_state.spark.COLOR); CGL();
	glVertexAttribPointer(    wave_state.spark.POSITION, 2, GL_FLOAT, GL_FALSE, 6 * sizeof(GLfloat), (GLvoid*)(0 * sizeof(GLfloat))); CGL();
	glVertexAttribPointer(    wave_state.spark.COLOR, 4, GL_FLOAT, GL_FALSE, 6 * sizeof(GLfloat), (GLvoid*)(2 * sizeof(GLfloat))); CGL();

	glDrawElements(GL_TRIANGLES, idata.size(), GL_UNSIGNED_INT, nullptr); CGL();
}

