precision mediump float;
varying float alpha;
uniform vec4 _ColorA;
uniform vec4 _ColorB;

void main(){
    gl_FragColor = mix(_ColorB, _ColorA, alpha);
}