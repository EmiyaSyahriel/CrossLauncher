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

uniform float _Time, _YScale;
uniform float _NormalStep;
uniform mat4 _Ortho;
uniform float _RngTrans, _RngDataA[16], _RngDataB[16];

varying float alpha;

const float PI = 3.141529653;
float animCurve(float x){
    return (1.0 + sin(PI * (x - 0.5))) * 0.5;
}
const float _XScale = 1.0;

float RNGL(float min, float max, int i){
    return mix(min, max, mix(_RngDataA[i], _RngDataB[i], animCurve(_RngTrans)));
}

vec3 calcWave(float x, float z){
    float sx = _XScale * x;
    float wave_1 = sin((sx * RNGL(1.5, 1.2, 0)) + (z * RNGL(0.90, 1.10, 8)) + (_Time * 0.25));
    float wave_2 = sin((sx * RNGL(3.0, 1.5, 1)) + z + (_Time * 0.75));
    float wave_3 = sin(( z * RNGL(10.5, 8.5, 2) + cos(_Time * 0.5)) * mix(0.5, 1.0, cos(sx + (_Time * 0.25))));
    float wave_4 = cos((sx * RNGL(1.0, 1.5, 5)) + (z * RNGL(5.5, 3.4, 5)) + (_Time * 1.12)) * mix(0.2, 1.1, cos(sx + (_Time * 0.15)));
    float wave_5 = cos((sx * RNGL(2.2, 1.8, 4)) + (z * RNGL(7.2, 2.5, 6)) + (_Time * 1.2)) * mix(0.35, 0.8, cos(sx + (_Time * 0.35)));
    float wy =
    (wave_1 * RNGL(1.00, 0.90,2)) +
    (wave_2 * RNGL(0.65, 0.75,3)) +
    (wave_3 * RNGL(0.55, 0.25,4)) +
    (wave_4 * RNGL(0.37, 0.15,9)) +
    (wave_5 * RNGL(0.21, 0.05,7));
    return vec3(x, wy, z);
}

void main() {
    vec4 cvpos = vec4( calcWave( POSITION.x, POSITION.y ), 1.0 );
    gl_Position = vec4(cvpos.x, (cvpos.y * 0.125) * _YScale, cvpos.z, 1.0) * _Ortho;

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
})EMBEDRES";
}
