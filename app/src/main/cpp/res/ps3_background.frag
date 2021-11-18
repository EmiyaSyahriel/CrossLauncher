precision lowp float;
#define lerp(a,b,t) (a + ((b - a) * t))
#define nrange(a) ((a + 1) / 2)

uniform vec3 _ColorA;
uniform vec3 _ColorB;
uniform sampler2D _Night;
uniform sampler2D _Day;
uniform float _TimeOfDay;

varying vec2 screenPos;

void main(){
    float color = lerp(texture2D(_Night, screenPos), texture2D(_Day, screenPos), _TimeOfDay).r;
    vec3 cA, cB;
    float t = color;

    if(color < 0.33){ 
        cA = vec3(0.0, 0.0, 0.0) ; 
        cB = _ColorA;
        t = color / 0.33;
    }
    if(color >= 0.33 && color < 0.66){
        cA = _ColorA; 
        cB = _ColorB;
        t = ((color - 0.33) / 0.33);
    }
    if(color >= 0.66){ 
        cA = _ColorB;
        cB = vec3(1.0, 1.0, 1.0);
        t = ((color - 0.66) / 0.33);
    }

    gl_FragColor = vec4(lerp(cA, cB, t), 1.0);
}
