precision highp float;
attribute vec3 POSITION;
attribute vec3 TEXCOORD1;

uniform float _Time, _YScale;
uniform mat4 _Ortho;
uniform float _RngTrans, _RngDataA[6], _RngDataB[6];

varying float alpha;

float rngi(int index) {
    return mix(_RngDataA[index], _RngDataB[index], _RngTrans);
}

float rngi3(int i1, int i2, int i3) {
    return mix(rngi(i1), rngi(i2), rngi(i3));
}

float mxrngi3(float a, float b, int i1, int i2, int i3) {
    return mix(a,b,rngi3(i1,i2,i3));
}

float calc_wave(float x, float t){
    float wave_a = sin((x * mxrngi3(0.5,1.5,0,4,3)) + (_Time * 0.50) + t);
    float wave_b = cos((x * mxrngi3(2.0,3.0,1,5,2)) + (_Time * 0.25) + t);
    float wave_c = cos((x * mxrngi3(4.0,5.0,2,3,1)) + (_Time * 1.00) + t);
    return (
        (wave_a * mxrngi3(0.400, 0.600, 1,0,3)) +
        (wave_b * mxrngi3(0.200, 0.300, 3,2,5)) +
        (wave_c * mxrngi3(0.150, 0.100, 2,1,4))
    ) * 0.125;
}

void main(){
    vec4 cvpos = vec4(
        POSITION.x,
        (POSITION.y + mix(0.0, calc_wave(POSITION.x, TEXCOORD1.y), TEXCOORD1.z)) * _YScale,
        POSITION.z,
        1.0);
    gl_Position= cvpos * _Ortho;
    alpha = TEXCOORD1.x;
}