precision lowp float;
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
