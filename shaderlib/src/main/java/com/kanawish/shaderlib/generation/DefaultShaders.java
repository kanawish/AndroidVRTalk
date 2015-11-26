package com.kanawish.shaderlib.generation;

/**
 * Created by kanawish on 2015-07-22.
 *
 * Constant strings of backup shaders that can be used when nothing else is available.
 *
 */
public class DefaultShaders {

    // From `/assets/shaders/default.vs'
    public static final String BACKUP_VERTEX_SHADER =
        "#ifdef GL_ES\n"+
        "precision mediump float;\n"+
        "#endif\n"+
        "\n"+
        "// Shader Versioning Scheme\n"+
        "uniform bool uKv1;\n"+
        "\n"+
        "// From a full example found here: http://www.learnopengles.com/android-lesson-four-introducing-basic-texturing/\n"+
        "\n"+
        "uniform mat4 uMVPMatrix;       // A constant representing the combined model/view/projection matrix.\n"+
        "uniform mat4 uMVMatrix;        // A constant representing the combined model/view matrix.\n"+
        "\n"+
        "attribute vec4 aPosition;      // Per-vertex position information we will pass in.\n"+
        "attribute vec3 aNormal;        // Per-vertex normal information we will pass in.\n"+
        "attribute vec4 aColor;         // Per-vertex color information we will pass in.\n"+
        "attribute vec2 aTexCoordinate; // Per-vertex texture coordinate information we will pass in.\n"+
        "\n"+
        "varying vec3 vPosition;        // This will be passed into the fragment shader.\n"+
        "varying vec3 vNormal;          // This will be passed into the fragment shader.\n"+
        "varying vec4 vColor;           // This will be passed into the fragment shader.\n"+
        "varying vec2 vTexCoordinate;   // This will be passed into the fragment shader.\n"+
        "\n"+
        "// The entry point for our vertex shader.\n"+
        "void main()\n"+
        "{\n"+
        "    // Transform the vertex into eye space.\n"+
        "    vPosition = vec3(uMVMatrix * aPosition);\n"+
        "\n"+
        "    // Pass through the color.\n"+
        "    vColor = aColor;\n"+
        "\n"+
        "    // Pass through the texture coordinate.\n"+
        "    vTexCoordinate = aTexCoordinate;\n"+
        "\n"+
        "    // Transform the normal's orientation into eye space.\n"+
        "    vNormal = vec3(uMVMatrix * vec4(aNormal, 0.0));\n"+
        "\n"+
        "    // gl_Position is a special variable used to store the final position.\n"+
        "    // Multiply the vertex by the matrix to get the final point in normalized screen coordinates.\n"+
        "    gl_Position = uMVPMatrix * aPosition;\n"+
        "}";


    public static final String BACKUP_FRAGMENT_SHADER =
        "\n" +
            "#ifdef GL_ES\n" +
            "precision mediump float;\n" +
            "#endif\n" +
            "\n" +
            "uniform vec2 uResolution;\n" +
            "uniform mat4 uEyeView;\n" +
            "uniform vec3 uLightPos;\n" +
            "uniform float uTime;\n" +
            "\n" +
            "uniform int uMode;\n" +
            "uniform vec3 uTranslate;\n" +
            "uniform vec4 uColor;\n" +
            "\n" +
            "uniform sampler2D uTexture0;\n" +
            "uniform sampler2D uTexture1;\n" +
            "uniform sampler2D uTexture2;\n" +
            "uniform sampler2D uTexture3;\n" +
            "\n" +
            "float rand(vec2 co)\n" +
            "{\n" +
            "    return fract(sin(dot(co.xy ,vec2(12.9898,78.233))) * 43758.5453);\n" +
            "}\n" +
            "\n" +
            "float fScene(vec3 p) {\n" +
            "\treturn length(p)-1.0;\n" +
            "}\n" +
            "\n" +
            "\n" +
            "void main() {\n" +
            "  gl_FragColor = vec4(1.0);//uColor - (rand(gl_FragCoord.xy/uResolution) * 0.10);\n" +
            "  //gl_FragColor.a = 1.0;\n" +
            "}\n";
}
