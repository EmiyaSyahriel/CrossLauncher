precision highp float;
attribute vec2 vpos;
attribute vec2 uv;
varying vec2 screenPos;
#define lerp(a,b,t) (a + ((b - a) * t))
#define nrange(a) ((a + 1) / 2)

uniform int month;

vec2 uv_data(){
	float x = (mod(float(month), 4.0)) / 4.0;
	float y = ((float(month) / 4.0)) / 3.0;
    x += uv.x / 4.0;
    y += uv.y / 3.0;
    return vec2(nrange(x), 1.0 - nrange(y));
}

void main(){
    screenPos = uv_data();
    gl_Position = vec4(vpos, 0.0, 1.0);
}
