#include "shaders.h"
//// AUTO-GENERATED RESOURCE FILE ////
//// File auto-generated using C# Script at "app/csx/embed_res.csx" ////
namespace R {
	const char* const blank_frag = R"EMBEDRES(precision lowp float;
void main(){ gl_FragColor = vec4(0.6, 0.0, 1, 1.0); }
)EMBEDRES";
	const char* const blank_vert = R"EMBEDRES(precision highp float;
attribute vec3 vpos;
attribute vec3 uv;
attribute vec3 normal;

void main(){
    gl_Position = vec4(vpos, 1.0);
}

)EMBEDRES";
	const char* const ps3_background_frag = R"EMBEDRES(precision lowp float;
float lerp(float a, float b, float t){ return a + ((b - a) * t); }
vec3 lerp3(vec3 a, vec3 b, float t){ return vec3(lerp(a.x, b.x, t), lerp(a.y, b.y, t), lerp(a.z, b.z, t)); }

uniform vec3 _ColorA;
uniform vec3 _ColorB;

varying vec2 screenPos;

void main(){
    gl_FragColor = vec4(lerp3(_ColorB, _ColorA, screenPos.y), 1.0);
}
)EMBEDRES";
	const char* const ps3_background_vert = R"EMBEDRES(precision highp float;
attribute vec2 vpos;
attribute vec2 uv;
varying vec2 screenPos;

void main(){
    screenPos = uv;
    gl_Position = vec4(vpos, 0.0, 1.0);
}
)EMBEDRES";
	const char* const ps3_sparkle_frag = R"EMBEDRES(precision lowp float;
varying vec4 f_vcol;

void main(){
    gl_FragColor = f_vcol;
}
)EMBEDRES";
	const char* const ps3_sparkle_vert = R"EMBEDRES(precision highp float;
attribute vec2 vpos;
attribute vec4 vcol;

uniform mat4 matrix;

varying vec4 f_vcol;

void main(){
    gl_Position = vec4(vpos, 0.0f, 1.0f) * matrix;
    f_vcol = vcol;
}
)EMBEDRES";
	const char* const ps3_wave_frag = R"EMBEDRES(precision lowp float;
varying float alpha;

float lerp(float a, float b, float t){ return a + ((b - a) * t); }
vec4 lerp4(vec4 a, vec4 b, float t){ return vec4(lerp(a.x, b.x, t), lerp(a.y, b.y, t), lerp(a.z, b.z, t), lerp(a.w, b.w, t)); }

uniform vec4 white;
uniform vec4 color;

float irange(float r){ return (min(1.0, max(0.0, r)) * 2.0) - 1.0; }
vec3 irange(vec3 r){ return vec3(irange(r.x),irange(r.y),irange(r.z)); }

// float ndl(){ return dot(vec3(0,0,1), abs(v2f_normal * v2f_normal));  }

void main(){
    gl_FragColor = lerp4(color, white, alpha);
    // gl_FragColor = vec4(v2f_normal, 1);
}
)EMBEDRES";
	const char* const ps3_wave_vert = R"EMBEDRES(precision highp float;
attribute vec3 position;

uniform float _Time;
uniform vec2 _ScreenSize;
uniform vec2 _RefSize;
uniform float _NormalStep;
uniform mat4 _Ortho;

varying float alpha;

float rRange(float x) {return (x * 2.0) - 1.0; }

vec3 calcWave(float x, float z){
    float bigwave = sin((x * 1.5) + (z * 0.75) + (_Time * 0.25));
    float med = sin((x * 3.0) + z + (_Time * 0.75));
    float xmed = sin((x * 2.5) + (z * 4.5) + (_Time * 1.0));

    med = rRange(med);
    xmed = rRange(xmed);

    xmed = max(xmed, 0.0);

    float smallwave = (med * xmed) * 0.05;
    float retval = (bigwave + smallwave) / 2.0;
    retval = rRange(retval);
    float y = (retval * 0.3) + 0.2;
    return vec3(x,y * 0.5f,z);
}

void main() {
    vec4 cvpos = vec4( calcWave( position.x, position.y ), 1.0 );
    gl_Position = cvpos * _Ortho;

    vec3 nrmx = calcWave( position.x + _NormalStep, position.y);
    vec3 nrmy = calcWave( position.x, position.y + _NormalStep);
    float edge = pow(abs(position.y), 4.0) * 1.5;
    alpha = 1.0 - abs( normalize( cross( nrmx, nrmy ) ).z );
    alpha = (1.0 - cos(pow(alpha, 4.0)));
    alpha = max(edge, alpha);
    // alpha = 0.5;
}
)EMBEDRES";
}
