precision lowp float;
#define lerp(a,b,t) (a + ((b - a) * t))
#define nrange(a) ((a + 1.0) / 2.0)
#define clamp(t,a,b) (min(max(t,a),b))

uniform vec3 _ColorA, _ColorB, _ColorC;
uniform sampler2D _Night;
uniform sampler2D _Day;
uniform float _TimeOfDay;
uniform int _UseTexture;

varying vec2 screenPos;

void main(){
    if(_UseTexture == 1){
        float color = mix(texture2D(_Night, screenPos), texture2D(_Day, screenPos), _TimeOfDay).r;
        vec3 cA, cB;
        float t = color;
        cA = _ColorA;
        cB = _ColorB;

        if(color < 0.5){
            cA = _ColorA;
            cB = _ColorB;
            t = color / 0.5;
        }else if(color >= 0.5){
            cA = _ColorB;
            cB = _ColorC;
            t = (color - 0.5) / 0.5;
        }

        gl_FragColor = vec4(mix(cA, cB, t), 1.0);
    } else {
        float color = (screenPos.x + screenPos.y) / 2.0;
        vec3 cA, cB;
        float t = color;
        cA = _ColorA;
        cB = _ColorC;

        if(color < 0.5){
            cA = _ColorA;
            cB = _ColorB;
            t = color / 0.5;
        }else if(color >= 0.5){
            cA = _ColorB;
            cB = _ColorC;
            t = (color - 0.5) / 0.5;
        }

        gl_FragColor = vec4(mix(cA, cB, t), 1.0);
    }
}
