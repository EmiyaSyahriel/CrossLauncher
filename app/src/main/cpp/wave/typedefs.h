
//
// Created by EmiyaSyahriel on 20/05/2021.
//
#pragma once
#include <glm/vec3.hpp> // glm::vec3
#include <glm/vec4.hpp> // glm::vec4
#include <glm/mat4x4.hpp> // glm::mat4
#include <glm/ext/matrix_transform.hpp> // glm::translate, glm::rotate, glm::scale
#include <glm/ext/matrix_clip_space.hpp> // glm::perspective
#include <glm/ext/scalar_constants.hpp> // glm::pi
typedef unsigned char byte;
class Vertex {
public:
    glm::vec3 position, normal;
    glm::vec2 uv;

    Vertex(glm::vec3 p){position = p;normal = glm::vec3(0,0,0); uv = glm::vec2 (0,0);} ;
};
