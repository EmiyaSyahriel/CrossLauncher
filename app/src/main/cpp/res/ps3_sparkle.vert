precision highp float;
attribute vec2 vpos;
attribute vec4 vcol;

uniform mat4 matrix;

varying vec4 f_vcol;

void main(){
    gl_Position = vec4(vpos, 0.0f, 1.0f) * matrix;
    f_vcol = vcol;
}
