precision highp float;
attribute vec3 POSITION;
attribute vec3 TEXCOORD1;
#define lerp(a,b,t) (a + ((b - a) * t))

uniform float _Time;
uniform mat4 _Ortho;

varying float alpha;

float calc_wave(float x, float t){
    float wave_a = sin((x * 1.0) + (_Time * 0.50) + t);
    float wave_b = cos((x * 2.7) + (_Time * 0.25) + t);
    float wave_c = cos((x * 4.3) + (_Time * 1.00) + t);
    return ((wave_a * 0.5) + (wave_b * 0.25) + (wave_c * 0.125)) * 0.125;
}

void main(){
    vec4 cvpos = vec4(
        POSITION.x,
        POSITION.y + lerp(0.0, calc_wave(POSITION.x, TEXCOORD1.y), TEXCOORD1.z),
        POSITION.z,
        1.0);
    gl_Position= cvpos * _Ortho;
    alpha = TEXCOORD1.x;
}