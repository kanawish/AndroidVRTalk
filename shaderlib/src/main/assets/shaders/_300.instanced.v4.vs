#version 300 es

#ifdef GL_ES
precision mediump float;
#endif

// Shader Versioning Scheme
uniform bool uKv1;

// From a full example found here: http://www.learnopengles.com/android-lesson-four-introducing-basic-texturing/
uniform mat4 uMMatrix;
uniform mat4 uVMatrix;
uniform mat4 uPMatrix;

uniform mat4 uMVPMatrix;       // A constant representing the combined model/view/projection matrix.
uniform mat4 uMVMatrix;        // A constant representing the combined model/view matrix.

in vec4 aPosition;      // Per-vertex position information we will pass in.
in vec3 aNormal;        // Per-vertex normal information we will pass in.
in vec2 aTexCoordinate; // Per-vertex texture coordinate information we will pass in.

in vec3 aTranslationHandle;
in vec3 aRotationHandle;
in vec3 aScaleHandle;
in vec4 aColor;         // Per-vertex color information we will pass in.
in vec4 aParametersHandle;

out vec4 vPosition;        // This will be passed into the fragment shader.
out vec3 vNormal;          // This will be passed into the fragment shader.
out vec2 vTexCoordinate;   // This will be passed into the fragment shader.

out vec4 vColor;           // This will be passed into the fragment shader.
out vec4 vParams;


mat4 s(vec3 s) {
    return mat4(
        s.x,0,0,0,
        0,s.y,0,0,
        0,0,s.z,0,
        0,0,0,1
    );
}

mat4 t(vec3 vt) {
    return mat4(
        1,0,0,0,
        0,1,0,0,
        0,0,1,0,
        vt.x,vt.y,vt.z,1
    );
}

mat4 rotx(float a) {
    float s = sin(a);
    float c = cos(a);

    return mat4(
        1.0,0.0,0.0,0.0,
        0.0,  c, -s,0.0,
        0.0,  s,  c,0.0,
        0.0,0.0,0.0,1.0
    );
}

mat4 rot(vec3 axis, float angle)
{
    axis = normalize(axis);
    float s = sin(angle);
    float c = cos(angle);
    float oc = 1.0 - c;
    
    return mat4(oc * axis.x * axis.x + c,           oc * axis.x * axis.y - axis.z * s,  oc * axis.z * axis.x + axis.y * s,  0.0,
                oc * axis.x * axis.y + axis.z * s,  oc * axis.y * axis.y + c,           oc * axis.y * axis.z - axis.x * s,  0.0,
                oc * axis.z * axis.x - axis.y * s,  oc * axis.y * axis.z + axis.x * s,  oc * axis.z * axis.z + c,           0.0,
                0.0,                                0.0,                                0.0,                                1.0);
}



// The entry point for our vertex shader.
void main()
{
    // FIXME START
    // vec4 offset = vec4(aTranslationHandle,0.0); // vec3(xOffset, yOffset, zOffset);
    // offset.z -= 12.0;

    // FIXME Transform the vertex into eye space.
    // vPosition = vec3(uMVMatrix * ((aPosition*1.)+offset));

    // FIXME: Pass the material color. 
    // vColor = vec4( float(gl_InstanceID+1%3)*0.5,float((gl_InstanceID+2)%3)*0.5,float((gl_InstanceID)%3)*0.5,1.0);
    // vColor = vec4(1.0,0.0,0.0,1.0) ;
    // vColor.x = aColor.x ;
    // vColor = aColor ;
    // vec4 patate = aColor; // + vec4(1.0,0.0,0.0,1.0);
    
    // gl_Position is a special variable used to store the final position.
    // Multiply the vertex by the matrix to get the final point in normalized screen coordinates.

    vec3 xA = vec3(1.,0.0,0.0); vec3 yA = vec3(0.,1.0,0.0); vec3 zA = vec3(0.,0.0,1.0);
    // Talking point: It's very powerful to get instant feedback to grasp how things work.
    mat4 r = rot(xA,aRotationHandle.x)*rot(yA,aRotationHandle.y)*rot(zA,aRotationHandle.z);
    mat4 s = s(aScaleHandle);
    mat4 t = t(aTranslationHandle-vec3(0.,0.,0.)); // patched...

    // Pass through the texture coordinate.
    vTexCoordinate = aTexCoordinate;
    vNormal = aNormal ;
    // FIXME END

    // The position of the light in **eye space**. 
    // TODO: Parametrize
    vec3 u_LightPos = vec3(uMVMatrix * vec4(0.0,1.0,-1.0,0.0)); 

    // Transform the vertex into eye space.
    vec3 modelViewVertex = vec3(uMVMatrix * t*r*aPosition);
    // Transform the normal's orientation into eye space.
    vec3 modelViewNormal = vec3(uMVMatrix * t*r*vec4(aNormal, 0.0));
    // Will be used for attenuation.
    float distance = length(u_LightPos - modelViewVertex) * 0.05;
    // Get a lighting direction vector from the light to the vertex.
    vec3 lightVector = normalize(u_LightPos - modelViewVertex);
	// Calculate the dot product of the light vector and vertex normal. If the 
	// normal and light vector are pointing in the same direction then it will
	// get max illumination.
    float lambertFactor = max(dot(modelViewNormal, lightVector), 0.1); 
	// Attenuate the light based on distance.
    float diffuse = lambertFactor * (1.0 / (1.0 + (0.25 * distance * distance))); 
	// Multiply the color by the illumination level. It will be interpolated 
	// across the triangle.
    vColor = aColor * diffuse;
    vParams = aParametersHandle ;
    // vColor = aColor * lambertFactor;

	// gl_Position is a special variable used to store the final position.
	// Multiply the vertex by the matrix to get the final point in normalized 
	// screen coordinates.		
	vPosition = vec4(t*r*s*aPosition);
    gl_Position = uMVPMatrix * t*r*s*aPosition;
}
