#include "shaders.h"
//// AUTO-GENERATED RESOURCE FILE ////
//// File auto-generated using C# Script at "app/csx/embed_res.csx" ////
namespace R {
	const char* const blank_frag = R"EMBEDRES(precision lowp float;
void main(){ gl_FragColor = vec4(0.6, 0.0, 1, 1.0); }
)EMBEDRES";
	const char* const ps3_background_frag = R"EMBEDRES(precision lowp float;
#define lerp(a,b,t) (a + ((b - a) * t))
#define nrange(a) ((a + 1.0) / 2.0)
#define clamp(t,a,b) (min(max(t,a),b))

uniform vec3 _ColorA, _ColorB, _ColorC;
uniform sampler2D _Night;
uniform sampler2D _Day;
uniform float _TimeOfDay;

varying vec2 screenPos;

void main(){
    float color = lerp(texture2D(_Night, screenPos), texture2D(_Day, screenPos), _TimeOfDay).r;
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

    gl_FragColor = vec4(lerp(cA, cB, t), 1.0);
}
)EMBEDRES";
	const char* const ps3_sparkle_frag = R"EMBEDRES(precision lowp float;
varying vec4 f_vcol;

void main(){
    gl_FragColor = f_vcol;
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
	const char* const psp_wave_frag = R"EMBEDRES(precision mediump float;
varying float alpha;
uniform vec4 _ColorA;
uniform vec4 _ColorB;

#define lerp(a,b,t) (a + ((b - a) * t))
#define lerp4(a,b,t) vec4(lerp(a.x, b.x, t), lerp(a.y, b.y, t), lerp(a.z, b.z, t), lerp(a.w, b.w, t))

void main(){
    gl_FragColor = lerp4(_ColorB, _ColorA, alpha);
})EMBEDRES";
	const char* const blank_vert = R"EMBEDRES(precision highp float;
attribute vec3 POSITION;
attribute vec3 TEXCOORD0;
attribute vec3 NORMAL;

void main(){
    gl_Position = vec4(POSITION, 1.0);
}

)EMBEDRES";
	const char* const ps3_background_vert = R"EMBEDRES(precision highp float;

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
	const char* const ps3_wave_vert = R"EMBEDRES(precision highp float;
attribute vec3 POSITION;
#define lerp(a,b,t) (a + ((b - a) * t))
#define sRange(x) ((x + 1.0) / 2.0)
#define rRange(x) ((x * 2.0) - 1.0)

uniform float _Time;
uniform float _NormalStep;
uniform mat4 _Ortho;

varying float alpha;

vec3 calcWave(float x, float z){
    float bigwave = sin((x * 1.5) + (z * 0.90) + (_Time * 0.25));
    float med = sin((x * 3.0) + z + (_Time * 0.75));
    float xmed = sin((z * 10.5) + cos(_Time * 0.5)) * lerp(0.5, 1.0, cos(x + (_Time * 0.25)));

    med = rRange(med);
    xmed = rRange(xmed);
    // xmed = max(xmed, 0.0);

    float smallwave = (med * xmed) * 0.05;
    float retval = (bigwave + smallwave) / 2.0;
    retval *= lerp(0.5, 1.5, sRange(cos(2.0 * x)) );
    retval = rRange(retval);
    float y = (retval * 0.3) + 0.2;
    return vec3(x,y * 0.5,z);
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
	const char* const psp_wave_vert = R"EMBEDRES(precision highp float;
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
})EMBEDRES";
}
