precision lowp float;
float lerp(float a, float b, float t){ return a + ((b - a) * t); }
vec3 lerp3(vec3 a, vec3 b, float t){ return vec3(lerp(a.x, b.x, t), lerp(a.y, b.y, t), lerp(a.z, b.z, t)); }

uniform vec3 _ColorA;
uniform vec3 _ColorB;

varying vec2 screenPos;

void main(){
    gl_FragColor = vec4(lerp3(_ColorB, _ColorA, screenPos.y), 1.0);
}
