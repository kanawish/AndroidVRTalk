package com.kanawish.shaderlib.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES30;
import android.opengl.GLUtils;
import android.util.Log;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.List;

import timber.log.Timber;

/**
 * uniform mat4 u_MVPMatrix;       // A constant representing the combined model/view/projection matrix.
 * uniform mat4 u_MVMatrix;        // A constant representing the combined model/view matrix.
 * <p/>
 * attribute vec4 a_Position;      // Per-vertex position information we will pass in.
 * attribute vec4 a_Color;         // Per-vertex color information we will pass in.
 * attribute vec3 a_Normal;        // Per-vertex normal information we will pass in.
 * attribute vec2 a_TexCoordinate; // Per-vertex texture coordinate information we will pass in.
 */
public class SimpleGLUtils {
    private static final String TAG = SimpleGLUtils.class.getSimpleName();

    public static final String U_MVPMATRIX = "u_MVPMatrix";
    public static final String U_MVMATRIX = "u_MVMatrix";

    public static final String A_POSITION = "a_Position";
    public static final String A_COLOR = "a_Color";
    public static final String A_NORMAL = "a_Normal";
    public static final String A_TEX_COORDINATE = "a_TexCoordinate";


    public static final int BYTES_PER_SHORT = 2;
    public static final int BYTES_PER_FLOAT = 4;

    public static final int COORDS_PER_VERTEX = 3;
    public static final int VERTEX_STRIDE = COORDS_PER_VERTEX * BYTES_PER_FLOAT; // 4 bytes per vertex coord (float)


    public static final FloatBuffer createFloatBuffer(float[] floats) {
        ByteBuffer bb = ByteBuffer.allocateDirect(
            // (# of coordinate values * 4 bytes per float)
            floats.length * BYTES_PER_FLOAT);
        bb.order(ByteOrder.nativeOrder());
        FloatBuffer floatBuffer = bb.asFloatBuffer();
        floatBuffer.put(floats);
        floatBuffer.position(0);
        return floatBuffer;
    }

    public static final ShortBuffer createFloatBuffer(short[] shorts) {
            // initialize byte buffer for the draw list
        ByteBuffer dlb = ByteBuffer.allocateDirect(
            // (# of coordinate values * 2 bytes per short)
            shorts.length * BYTES_PER_SHORT);
        dlb.order(ByteOrder.nativeOrder());
        ShortBuffer shortBuffer = dlb.asShortBuffer();
        shortBuffer.put(shorts);
        shortBuffer.position(0);
        return shortBuffer;
    }

    /**
     * @return the program handle to be used by our model objects.
     */
    public static final int loadGLProgram(String vertexShaderCode, String fragmentShaderCode) throws ShaderCompileException {

        int vertexShaderHandle = SimpleGLUtils.loadShader(GLES30.GL_VERTEX_SHADER, vertexShaderCode);
        int fragmentShaderHandle = SimpleGLUtils.loadShader(GLES30.GL_FRAGMENT_SHADER, fragmentShaderCode);

        int programHandle = GLES30.glCreateProgram();             // create empty OpenGL Program
        SimpleGLUtils.checkGlErrorRTE("glCreateProgram");
        GLES30.glAttachShader(programHandle, vertexShaderHandle);   // add the vertex shader to program
        SimpleGLUtils.checkGlErrorRTE("glAttachShader vertex");
        GLES30.glAttachShader(programHandle, fragmentShaderHandle); // add the fragment shader to program
        SimpleGLUtils.checkGlErrorRTE("glAttachShader frag");
        GLES30.glLinkProgram(programHandle);                  // create OpenGL program executables
        SimpleGLUtils.checkGlErrorRTE("glLinkProgram");

        return programHandle;
    }

    /**
     * Utility method for compiling a OpenGL shader.
     *
     * @param type       - Vertex or fragment shader type.
     * @param shaderCode - String containing the shader code.
     * @return - Returns an id for the shader.
     */
    static public int loadShader(int type, String shaderCode) throws ShaderCompileException {
        // create a vertex shader type (GLES30.GL_VERTEX_SHADER)
        // or a fragment shader type (GLES30.GL_FRAGMENT_SHADER)
        int shader = GLES30.glCreateShader(type);
        SimpleGLUtils.checkGlErrorRTE(String.format("glCreateShader %d", type));

        // add the source code to the shader and compile it
        GLES30.glShaderSource(shader, shaderCode);
        SimpleGLUtils.checkGlErrorRTE(String.format("GLES30.glShaderSource(shader, shaderCode): %s", shaderCode));
        GLES30.glCompileShader(shader);
        SimpleGLUtils.checkGlErrorRTE("GLES30.glCompileShader(shader)");

        // Get the compilation status.
        final int[] compileStatus = new int[1];
        GLES30.glGetShaderiv(shader, GLES30.GL_COMPILE_STATUS, compileStatus, 0);

        String errorMessage = "Error creating shader.";
        // If the compilation failed, delete the shader.
        if (compileStatus[0] == 0) {
            errorMessage = "Error compiling shader:\n " + GLES30.glGetShaderInfoLog(shader);
            GLES30.glDeleteShader(shader);
            shader = 0;
        }

        if (shader == 0) {
            Log.e(TAG, errorMessage);
            throw new ShaderCompileException(errorMessage);
        }

        return shader;
    }

    static public int loadShaderAssetFile(Context context, int type, String filename) throws IOException, ShaderCompileException {
        String shaderCode = IOUtils.loadStringFromAsset(context, filename);
        return SimpleGLUtils.loadShader(type, shaderCode);
    }

    /**
     * Utility method for debugging OpenGL calls. Provide the name of the call
     * just after making it:
     * <p/>
     * <pre>
     * mColorHandle = GLES30.glGetUniformLocation(mProgram, "vColor");
     * MyGLRenderer.checkGlErrorRTE("glGetUniformLocation");</pre>
     *
     * If the operation is not successful, the check throws a runtime exception.
     *
     * @param label - Identifies what type of check we're doing.
     */
    public static void checkGlErrorRTE(String label) {
        int error;
        while ((error = GLES30.glGetError()) != GLES30.GL_NO_ERROR) {
            Timber.e("%s : %s error", label, error);
            throw new RuntimeException(label + ": glError " + error);
        }
    }

    /**
     * Version of gl error check that throws a Checked Exception.
     *
     * @param glOperation - Identifies what type of check we're doing.
     */
    public static void checkGlErrorCE(String glOperation) {
        int error;
        while ((error = GLES30.glGetError()) != GLES30.GL_NO_ERROR) {
            String message = String.format("%s: glError %d (%05X)", glOperation, error, error);
            Log.e(TAG, message);
            return;
        }
    }

    /**
     * Returns a texture handle
     * <p/>
     * see http://www.learnopengles.com/android-lesson-four-introducing-basic-texturing/
     */
    public static int loadTexture(final Context context, final int resourceId) {
        final int[] textureHandle = new int[1];

        GLES30.glGenTextures(1, textureHandle, 0);

        if (textureHandle[0] != 0) {
            final BitmapFactory.Options options = new BitmapFactory.Options();
            options.inScaled = false;    // No pre-scaling

            // Read in the resource
            final Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), resourceId, options);

            // Bind to the texture in OpenGL
            GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, textureHandle[0]);

            // Set filtering
            GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MIN_FILTER, GLES30.GL_NEAREST);
            GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MAG_FILTER, GLES30.GL_NEAREST);

            // Load the bitmap into the bound texture.
            GLUtils.texImage2D(GLES30.GL_TEXTURE_2D, 0, bitmap, 0);

            // Recycle the bitmap, since its data has been loaded into OpenGL.
            bitmap.recycle();
        }

        if (textureHandle[0] == 0) {
            throw new RuntimeException("Error loading texture.");
        }

        return textureHandle[0];
    }

    public static int loadTexture(final Context context, final int [] resourceIds) {
        final int[] textureHandle = new int[1];

        GLES30.glGenTextures(1, textureHandle, 0);

        if (textureHandle[0] != 0) {
            final BitmapFactory.Options options = new BitmapFactory.Options();
            options.inScaled = false;    // No pre-scaling

            // Read in the resource
            final Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), resourceIds[0], options);

            // Bind to the texture in OpenGL
            GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, textureHandle[0]);

            // Set filtering
            GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MIN_FILTER, GLES30.GL_NEAREST);
            GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MAG_FILTER, GLES30.GL_NEAREST);

            // Load the bitmap into the bound texture.
            GLUtils.texImage2D(GLES30.GL_TEXTURE_2D, 0, bitmap, 0);

            // Recycle the bitmap, since its data has been loaded into OpenGL.
            bitmap.recycle();
        }

        if (textureHandle[0] == 0) {
            throw new RuntimeException("Error loading texture.");
        }

        return textureHandle[0];
    }
}
