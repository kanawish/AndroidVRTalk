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

#define PI 3.1416
#define M_PI 3.14159265358979323846
#define rotation mat2(cos(M_PI/4.0), sin(M_PI/4.0), -sin(M_PI/4.0), cos(M_PI/4.0))

void main() {

    vec3 fwd = uEyeView[2].xyz;
	vec3 up = vec3(-uEyeView[1].x,uEyeView[1].y,uEyeView[1].z);
	vec3 right = vec3(uEyeView[0].x,-uEyeView[0].y,uEyeView[0].z);

    // We use this to divide our space in a 100x100 pixel repeated grid
	float gridSize = 100.0 ;
    // Find out what our *grid relative screen pixel* position is.
	vec2 pos = mod(gl_FragCoord.xy, vec2(gridSize)) - vec2(gridSize/2.0) ;

	// Optional: rotate the relative position.
// 	vec2 pos = mod(rotation*gl_FragCoord.xy, vec2(gridSize)) - vec2(gridSize/2.0) ;
	
	// DISTANCE FUNCTION: How far are we from 0,0 ?
	float dist_squared = dot(pos, pos);

    vec4 col1 = vec4(.9, .7, .9, 1.0);
    vec4 col2 = vec4(.20, .20, .40, 1.0);
    
    // Pick between color 1 and color 2, 
	out_color = mix(col1, col2, smoothstep(1500.0, 2000.0, dist_squared));
}

