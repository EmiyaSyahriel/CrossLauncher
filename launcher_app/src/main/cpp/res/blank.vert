precision highp float;
attribute vec3 POSITION;
attribute vec3 TEXCOORD0;
attribute vec3 NORMAL;

void main(){
    gl_Position = vec4(POSITION, 1.0);
}

