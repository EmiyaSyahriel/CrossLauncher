attribute vec3 vpos;
attribute vec2 uv;
varying vec2 screenPos;

void main(){
    screenPos = uv;
    gl_Position = vec4(vpos, 1.0);
}