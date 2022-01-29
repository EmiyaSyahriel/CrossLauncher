precision mediump float;
varying float alpha;
uniform vec4 _ColorA;
uniform vec4 _ColorB;

#define lerp(a,b,t) (a + ((b - a) * t))
#define lerp4(a,b,t) vec4(lerp(a.x, b.x, t), lerp(a.y, b.y, t), lerp(a.z, b.z, t), lerp(a.w, b.w, t))

void main(){
    gl_FragColor = lerp4(_ColorB, _ColorA, alpha);
}