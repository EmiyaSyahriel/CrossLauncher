precision highp float;
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
