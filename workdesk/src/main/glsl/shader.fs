// Fragment Shader Y.4
#version 300 es

#ifdef GL_ES
precision mediump float;
#endif

uniform vec2 uResolution;
uniform mat4 uEyeView;
uniform vec3 uLightPos;
uniform float uTime;

uniform int uMode;
// uniform vec3 uTranslate;
uniform vec4 uColor;

uniform sampler2D uTexture0;
uniform sampler2D uTexture1;
uniform sampler2D uTexture2;
uniform sampler2D uTexture3;

// in vec4 vTranslation;
in vec4 vPosition;
in vec3 vNormal; 
in vec2 vTexCoordinate;
in vec4 vColor;
in vec4 vRawColor;
in vec4 vParams;
in vec3 vTranslation;  

out vec4 out_color;

float rand(vec2 co)
{
    return fract(sin(dot(co.xy ,vec2(12.9898,78.233))) * 43758.5453);
}    

#define PI 3.1416
#define M_PI 3.14159265358979323846
#define rotation mat2(cos(M_PI/4.0), sin(M_PI/4.0), -sin(M_PI/4.0), cos(M_PI/4.0))

void polkaDotter() {
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


// https://www.shadertoy.com/view/lt2SR1
vec3 skyColor( in vec3 rd )
{
    vec3 sundir = normalize( vec3(.0, .1, 1.) );
    
    float yd = min(rd.y, 0.);
    rd.y = max(rd.y, 0.);
      
    vec3 col = vec3(0.);
    
    col += vec3(.4, .4 - exp( -rd.y*20. )*.3, .0) * exp(-rd.y*9.); // Red / Green 
    col += vec3(.3, .5, .6) * (1. - exp(-rd.y*8.) ) * exp(-rd.y*.9) ; // Blue
    
    col = mix(col*1.2, vec3(.3),  1.-exp(yd*100.)); // Fog
    
    col += vec3(1.0, .8, .55) * pow( dot(rd,sundir), 15. ) * .6; // Sun
    col += pow(dot(rd, sundir), 150.0) *.15;
    
    return col;
}

float sdSphere( vec3 p, float s )
{
  return length(p)-s;
}

float fields( vec3 p ) {
	// a sphere at coords 
	float d = sdSphere(p-vTranslation,0.9);
	return d ;
}

// From pouet http://www.pouet.net/topic.php?which=7920&page=21
// find the normal, passing in 'contact' point.
vec3 calcNormal( in vec3 pos )
{
    vec3 e = vec3(0.2, 0.00, 0.00);
    vec3 n;
    n.x = fields(pos + e.xyy) - fields(pos - e.xyy);
    n.y = fields(pos + e.yxy) - fields(pos - e.yxy);
    n.z = fields(pos + e.yyx) - fields(pos - e.yyx);
    return normalize(n);
}

vec3 calcNormalTest( in vec3 pos ) {
	return normalize(pos - vec3(0.,0.,-10.));
}

const vec4 ambientLight = vec4(0.05);//5, 0.5, 0.5, 1.0);
// TODO: Found a few issues with -y, debug.
const vec3 lightPos = vec3(-1.5,10.,-10.); // TODO: make dynamic
const vec4 light = vec4(1.0,1.0,1.0,1.0);

vec4 shade(vec3 p)
{
    vec3 normal = calcNormalTest(p);
    float lightIntensity = 0.2;
	float shadow = 1.; //getShadow(p, lightPos, 16.0);
	// if(shadow > 0.0) // If we are at all visible
	// {
		vec3 lightDirection = normalize(lightPos - p);
		lightIntensity = shadow * clamp(dot(normal, lightDirection), 0.0, 1.0);
	// }
	return light * lightIntensity  + ambientLight * (1.0 - lightIntensity);
}

// Bad example, but just to prove theory.
// Put sphere at 0,1,-3
vec4 march( in vec3 rd ) {
	const int maxSteps = 4;
	const float epsilon = 0.01;

	// March
	float t = 0.96;
    for(int i = 0; i < maxSteps; ++i) {
    	vec3 p = -vec3(0.0,0.0,1.0)+(rd * t);
        float d = fields(p);
        if(d < epsilon) {
        	return vRawColor * shade(p);
        }
        t += d ;  
    }
    // return vec4(1.0,1.0,0.0,1.0); 
 	discard ;
}

void main() {
//   out_color = vColor+vec4(0.0,1.0,0.0,1.0);
    // Camera Ray
    vec3 dir = normalize(vec3(vPosition.xy,abs(vPosition.z)));

    if( vParams.x == 4.0 ) {
        polkaDotter();
    } else if(vParams.x == 5.0 ) { 
    	// *** TODO Demo 
    	out_color = march(vPosition.xyz); 
    } else if ( vParams.x == 2.0 ) {
        // out_color = vec4(mod(vPosition.x,1.0),mod(vPosition.y,1.0),0.0,0.0);
        // if(  
        //     step(0.04,mod(vPosition.x,3.0))==0.0 || 
        //     step(0.04,mod(vPosition.z,3.0))==0.0 ||
        //     step(0.04,mod(vPosition.y,3.0))==0.0 
        // )  
        //     out_color = vColor ;
        // else 
        out_color = vec4(0.0,0.4,0.0,1.0);
        // *** TODO Comment out to demo the sky, 
        // Explain in 2s the function code
        out_color = vec4(skyColor(dir),1.0);
    }
    else {
        out_color = vec4(1.0,0.0,0.0,1.0);
        out_color = vColor;
        // *** TODO Demo shape drawing, meh... 
        // polkaDotter();
    }
        
    // out_color += vec4(vPosition.x,vPosition.y,0.0,0.0); 
  //out_color = vColor ; // - (rand(gl_FragCoord.xy/uResolution) * 0.10);
  // gl_FragColor.a = 1.0;
}
