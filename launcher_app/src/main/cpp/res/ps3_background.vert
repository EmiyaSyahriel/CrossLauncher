precision highp float;

attribute vec2 POSITION;
attribute vec2 TEXCOORD0;
varying vec2 screenPos;
uniform int _Month;

#define lerp(a,b,t) (a + ((b - a) * t))
#define nrange(a) ((a + 1.0) / 2.0)

vec2 uv_data(){
	float x = float(_Month) / 12.0;
	float y = 0.0;
    x += TEXCOORD0.x / 6.0;
    y += TEXCOORD0.y;
    return vec2(x, y);
}

void main(){
    screenPos = uv_data();
    gl_Position = vec4(POSITION, 0.0, 1.0);
}
