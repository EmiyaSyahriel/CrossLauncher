precision highp float;
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
