precision lowp float;

attribute vec3 position;

uniform float _Time;
uniform float _YScale;
uniform float _NormalStep;

varying float alpha;

float rRange(float x) {return (x * 2.0) - 1.0; }

vec3 calcWave(float x, float z){
    float bigwave = sin(x + (z * 0.5) + (_Time * 0.25F));
    float med = sin((x * 2.0) + z + (_Time * 1.0F));
    float xmed = sin((x * 1.5F) + (z * 4.0F) + (_Time * 1.2F));

    med = rRange(med);
    xmed = rRange(xmed);

    xmed = max(xmed, 0.0);

    float smallwave = (med * xmed) * 0.05;
    float retval = (bigwave + smallwave) / 2.0;
    retval = rRange(retval);
    float y =(retval * 0.3) + 0.2;
    return vec3(x,y * _YScale,z);
}

void main() {
    gl_Position = vec4( calcWave( position.x, position.y ), 1.0 );

    vec3 nrmx = calcWave( position.x + _NormalStep, position.y ) - gl_Position.xyz;
    vec3 nrmy = calcWave( position.x, position.y + _NormalStep ) - gl_Position.xyz;
    alpha = 1.0 - abs( normalize( cross( nrmx, nrmy ) ).z );
    alpha = (1.0 - cos( alpha * alpha )) * _YScale;
}
