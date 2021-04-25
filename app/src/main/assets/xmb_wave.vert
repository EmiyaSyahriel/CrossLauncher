precision lowp float;
attribute vec3 position;
attribute vec3 normal;

uniform float _Time;

varying vec3 v2f_pos;
varying vec3 v2f_normal;

void main(){
    v2f_pos = position;
    v2f_normal = normal;
    gl_Position = vec4(v2f_pos, 1.0);
}