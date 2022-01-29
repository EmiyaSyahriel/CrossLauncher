precision highp float;
attribute vec2 POSITION;
attribute vec4 COLOR;

uniform mat4 _Ortho;

varying vec4 f_vcol;

void main(){
    gl_Position = vec4(POSITION, 0.0, 1.0) * _Ortho;
    f_vcol = COLOR;
}
