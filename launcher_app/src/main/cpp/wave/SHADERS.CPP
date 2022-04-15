#include "SHADERS.HPP"
//// AUTO-GENERATED RESOURCE FILE ////
//// File auto-generated using C# Script at "app/csx/embed_res.csx" ////
namespace R {
	const char* const blank_frag = R"EMBEDRES(precision lowp float;
void main(){ gl_FragColor = vec4(0.6, 0.0, 1, 1.0); }
)EMBEDRES";
	const char* const blank_vert = R"EMBEDRES(precision highp float;
attribute vec3 POSITION;
attribute vec3 TEXCOORD0;
attribute vec3 NORMAL;

void main(){
    gl_Position = vec4(POSITION, 1.0);
}

)EMBEDRES";
	const char* const ps3_background_frag = R"EMBEDRES(precision lowp float;
#define nrange(a) ((a + 1.0) / 2.0)
#define clamp(t,a,b) (min(max(t,a),b))

uniform vec3 _ColorA, _ColorB, _ColorC;
uniform sampler2D _Night;
uniform sampler2D _Day;
uniform float _TimeOfDay;
uniform int _UseTexture;

varying vec2 screenPos;

void main(){
    if(_UseTexture == 1){
        float color = mix(texture2D(_Night, screenPos), texture2D(_Day, screenPos), _TimeOfDay).r;
        vec3 cA, cB;
        float t = color;
        cA = _ColorA;
        cB = _ColorB;

        if(color < 0.5){
            cA = _ColorA;
            cB = _ColorB;
            t = color / 0.5;
        }else if(color >= 0.5){
            cA = _ColorB;
            cB = _ColorC;
            t = (color - 0.5) / 0.5;
        }

        gl_FragColor = vec4(mix(cA, cB, t), 1.0);
    } else {
        float color = (screenPos.x + screenPos.y) / 2.0;
        vec3 cA, cB;
        float t = color;
        cA = _ColorA;
        cB = _ColorC;

        if(color < 0.5){
            cA = _ColorA;
            cB = _ColorB;
            t = color / 0.5;
        }else if(color >= 0.5){
            cA = _ColorB;
            cB = _ColorC;
            t = (color - 0.5) / 0.5;
        }

        gl_FragColor = vec4(mix(cA, cB, t), 1.0);
    }
}
)EMBEDRES";
	const char* const ps3_background_vert = R"EMBEDRES(precision highp float;

attribute vec2 POSITION;
attribute vec2 TEXCOORD0;
varying vec2 screenPos;
uniform int _Month;

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
)EMBEDRES";
	const char* const ps3_sparkle_frag = R"EMBEDRES(precision lowp float;
varying vec4 f_vcol;

void main(){
    gl_FragColor = f_vcol;
}
)EMBEDRES";
	const char* const ps3_sparkle_vert = R"EMBEDRES(precision highp float;
attribute vec2 POSITION;
attribute vec4 COLOR;

uniform mat4 _Ortho;

varying vec4 f_vcol;

void main(){
    gl_Position = vec4(POSITION, 0.0, 1.0) * _Ortho;
    f_vcol = COLOR;
}
)EMBEDRES";
	const char* const ps3_wave_frag = R"EMBEDRES(precision lowp float;
varying float alpha;
uniform vec4 _ColorA, _ColorB;

float lerp(float a, float b, float t){ return a + ((b - a) * t); }
vec4 lerp4(vec4 a, vec4 b, float t){ return vec4(lerp(a.x, b.x, t), lerp(a.y, b.y, t), lerp(a.z, b.z, t), lerp(a.w, b.w, t)); }

float irange(float r){ return (min(1.0, max(0.0, r)) * 2.0) - 1.0; }
vec3 irange(vec3 r){ return vec3(irange(r.x),irange(r.y),irange(r.z)); }

// float ndl(){ return dot(vec3(0,0,1), abs(v2f_normal * v2f_normal));  }

void main(){
    gl_FragColor = lerp4(_ColorB, _ColorA, alpha);
    // gl_FragColor = vec4(v2f_normal, 1);
}
)EMBEDRES";
	const char* const ps3_wave_vert = R"EMBEDRES(precision highp float;
attribute vec3 POSITION;

float sRange(float x) { return (((x) + 1.0) / 2.0); }
float rRange(float x) { return (((x) * 2.0) - 1.0); }

uniform float _Time;
uniform float _NormalStep;
uniform mat4 _Ortho;
uniform float _RngTrans, _RngDataA[5], _RngDataB[5];

varying float alpha;

float rngi(int index){
    return mix(_RngDataA[index], _RngDataB[index], _RngTrans);
}
float mxrngi(float a, float b, int index){
    return mix(a,b,rngi(index));
}

float avg(float a, float b, float z){
    return (a + b) / z;
}

vec3 calcWave(float x, float z){
    float wave_1 = sin((x * mxrngi( 1.5, 1.0,0)) + (z * mxrngi(1.10,0.90,1)) + (_Time * 0.25));
    float wave_2 = sin((x * mxrngi( 2.3, 1.9,3)) + (z * mxrngi(2.50,0.75,4)) + (_Time * 0.43));
    float wave_3 = sin((z * mxrngi( 9.5,15.9,2)) + (_Time * 2.34));
    float wave_4 = sin((x * mxrngi( 2.3, 0.9,1)) + (z * mxrngi(1.20,1.00,2)) + (_Time * 0.73));
    float wave_5 = sin((z * mxrngi(12.5,15.5,4)) + (_Time * 1.64)) ;
    float wave_6 = sin((x * mxrngi( 2.9, 1.0,2)) + (z * mxrngi(2.80,3.25,3)) + (_Time * 0.64));

    wave_2 = rRange(avg(wave_2, wave_6, 1.65));
    wave_3 = rRange(avg(wave_3, wave_5, 2.0) * mix(0.5, 1.0, cos(x + (_Time * 0.35))));
    // xmed = max(xmed, 0.0);

    wave_1 = avg(wave_1, wave_2, 1.25);

    float retval = (
        (wave_1 * mxrngi(0.650, 0.873, 0)) + 
        (wave_2 * mxrngi(0.140, 0.112, 2)) +
        (wave_3 * mxrngi(0.539, 0.301, 4))
        ) * mxrngi(0.15, 0.25, 2);
    retval *= mix(0.5, 1.5, sRange(cos(2.0 * x)));
    retval = rRange(retval);
    float y = 0.25 + (retval * 0.15);
    return vec3(x, y, z);
}

void main() {
    vec4 cvpos = vec4( calcWave( POSITION.x, POSITION.y ), 1.0 );
    gl_Position = cvpos * _Ortho;

    vec3 nrmx = calcWave( POSITION.x + _NormalStep, POSITION.y);
    vec3 nrmy = calcWave( POSITION.x, POSITION.y + _NormalStep) * 0.5;
    //  float edge = pow(abs(position.y), 4.0) * 1.5;
    alpha = 1.0 - abs( normalize( cross( nrmx, nrmy ) ).z );
    alpha = (1.0 - cos(pow(alpha, 2.0)));
    // alpha = max(edge, alpha);
    // alpha = 0.5;
}
)EMBEDRES";
	const char* const psp_wave_frag = R"EMBEDRES(precision mediump float;
varying float alpha;
uniform vec4 _ColorA;
uniform vec4 _ColorB;

void main(){
    gl_FragColor = mix(_ColorB, _ColorA, alpha);
})EMBEDRES";
	const char* const psp_wave_vert = R"EMBEDRES(precision highp float;
attribute vec3 POSITION;
attribute vec3 TEXCOORD1;

uniform float _Time;
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
        POSITION.y + mix(0.0, calc_wave(POSITION.x, TEXCOORD1.y), TEXCOORD1.z),
        POSITION.z,
        1.0);
    gl_Position= cvpos * _Ortho;
    alpha = TEXCOORD1.x;
})EMBEDRES";
}
