precision highp float;
attribute vec3 vpos;
attribute vec3 uv;
attribute vec3 normal;

void main(){
    gl_Position = vec4(vpos, 1.0);
}

