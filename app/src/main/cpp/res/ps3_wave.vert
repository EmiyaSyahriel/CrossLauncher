precision highp float;
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
