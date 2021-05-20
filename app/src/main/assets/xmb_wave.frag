precision lowp float;
varying vec3 v2f_pos;
varying vec3 v2f_normal;

float lerp(float a, float b, float t){ return a + ((b - a) * t); }
vec3 lerp3(vec3 a, vec3 b, float t){ return vec3(lerp(a.x, b.x, t), lerp(a.y, b.y, t), lerp(a.z, b.z, t)); }
vec4 lerp4(vec4 a, vec4 b, float t){ return vec4(lerp(a.x, b.x, t), lerp(a.y, b.y, t), lerp(a.z, b.z, t), lerp(a.w, b.w, t)); }

uniform vec4 white;
uniform vec4 color;

float ndl(){
    return dot(vec3(0,0,1), abs(v2f_normal));
}

void main(){
    gl_FragColor = lerp4(white, color, ndl());
    //gl_FragColor = vec4(v2f_normal, 1.0);
}