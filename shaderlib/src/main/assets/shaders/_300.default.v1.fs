#version 300 es

#ifdef GL_ES
precision mediump float;
#endif

uniform vec2 uResolution;
uniform mat4 uEyeView;
uniform vec3 uLightPos;
uniform float uTime;

uniform int uMode;
uniform vec3 uTranslate;
uniform vec4 uColor;

uniform sampler2D uTexture0;
uniform sampler2D uTexture1;
uniform sampler2D uTexture2;
uniform sampler2D uTexture3;

in vec4 vColor;

out vec4 out_color;

float rand(vec2 co)
{
    return fract(sin(dot(co.xy ,vec2(12.9898,78.233))) * 43758.5453);
}

float fScene(vec3 p) {
	return length(p)-1.0;
}


void main() {
//   out_color = vColor+vec4(0.0,1.0,0.0,1.0);
  out_color = vColor+vec4(0.0,1.0,0.0,1.0);
  //out_color = vColor ; // - (rand(gl_FragCoord.xy/uResolution) * 0.10);
  // gl_FragColor.a = 1.0;
}
