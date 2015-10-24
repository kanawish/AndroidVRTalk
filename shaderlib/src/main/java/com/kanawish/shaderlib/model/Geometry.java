package com.kanawish.shaderlib.model;

import android.opengl.GLES30;
import android.opengl.Matrix;
import android.os.SystemClock;

import com.kanawish.shaderlib.defaults.DefaultShaders;
import com.kanawish.shaderlib.utils.ShaderCompileException;
import com.kanawish.shaderlib.utils.SimpleGLUtils;
import com.kanawish.shaderlib.defaults.DefaultModels;

import java.nio.FloatBuffer;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created by kanawish on 2015-07-09.
 *
 * We need to be able to build new geometry on the fly, this class is to
 * help us do this.
 *
 * Initial idea is, for each object we'll want to manipulate as an item in 3d space,
 * we'll create one of these.
 *
 * NOTE: There's a lot of performance related concepts I don't yet know about with OpenGL.
 *
 * Meaning we might not be able to scale to big scenes with this, but it will get us started.
 *
 * NOTE: A few dumb war stories, how simple things can stall you.
 *
 * - Remove the / 3 on the vertex count.
 * - Any badly ordered or badly 'scoped' pipeline commands will bork it.
 *

 TODO: Complete this UML, see if it's valuable.

 @startuml

 class StereoRenderer {
 +onRendererShutdown()
 +onSurfaceChanged(width, height)
 +onSurfaceCreated(config)
 +onNewFrame(headTransform)
 +onDrawEye(eye)
 +onFinishFrame(viewport)
 }

 StereoRenderer *-- Geometry
 class Geometry {
     +initGlProgram()
     +initBuffers()
     +assignGLProgram()
     +initHandlers()
     +draw()
     +setVertexShaderCode()
     +setFragmentShaderCode()
 }

 @enduml

 */
public class Geometry implements Renderable {

    // Only making a map for 8 units for now.
    private static final int [] textureUnits = {
        GLES30.GL_TEXTURE0,
        GLES30.GL_TEXTURE1,
        GLES30.GL_TEXTURE2,
        GLES30.GL_TEXTURE3,
        GLES30.GL_TEXTURE4,
        GLES30.GL_TEXTURE5,
        GLES30.GL_TEXTURE6,
        GLES30.GL_TEXTURE7,
    };

    // **** Buffers
    private int vertexCount = 0 ;
    private FloatBuffer vertices;
    private FloatBuffer normals;
    // NOTE: Per-instance float buffers. See draw() for details.
    private int instancedCount = 1 ;
    private FloatBuffer translations;
    private FloatBuffer rotations;
    private FloatBuffer scales;
    private FloatBuffer parameters;

    // TODO: Implement
    private FloatBuffer colors; // Might want multiple?
    private FloatBuffer textureCoords; // Might want multiple?

    // **** Matrices
//    private float[] camera = new float[16]; // Outside our scope.
//    private float[] view = new float[16]; // Should be passed in. (actually is...)
    private float[] modelMatrix = new float[16]; // Our model's transformation.

    // **** Program
    private String vertexShaderCode;
    private String fragmentShaderCode;
    private int programHandle;

    // **** Handles
    private int uMVMatrixHandle;
    private int uMVPMatrixHandle;

    private int aPositionHandle;
    private int aNormalHandle;
    // NOTE: Per-instance handles. See draw() for details.
    private int aTranslationHandle;
    private int aRotationHandle;
    private int aScaleHandle;
    private int aParametersHandle; // We assume 4 extra params for vector shader would be enough for the moment.

    // TODO: Implement
    private int aColorHandle;

    private int aTextureCoordinateHandle;

    // **** Shader parameter Handlers
    private int uResolution;
    private int uEyeViewMatrix;
    private int uLightPosHandle;

    private int uTime;
    private int uMode;

    private int uColor;

    private int [] uTextureUniformHandles = new int [8]; // Used to pass in the texture.

    private int [] textureDataHandles; // Handle to our texture data. (See loader helper method)

    // **** Actual data to be passed via handles
    private float[] modelViewMatrix4fv = new float[16];

    private float[] modelViewProjectionMatrix4fv = new float[16];
    private int mode1i;

    private float[] eyeViewMatrix4fv = new float[16];
    // In seconds, initialized to 0 on first call to draw.
    float time1f = -1;
    private float[] resolution2fv = new float[] {256,256}; // Default value to help if we forget to assign this.

    private float[] lightPos3fv = new float[3];

    // Model transforms
    private float[] modelTranslation = new float[]{0, -1f, -1.0f};
    private float[] modelRotation = new float[]{0, 0, 0};
    private float modelScale = 1.0f ;


    // "Feeders"
    // NOTE: We'll try to only have 1 enqueued item at a time, and we *must* measure perf here.
    // NOTE: To mitigate performance impact from the get-go, we'll simply set the polling freq. at 1~2 seconds.
    private ConcurrentLinkedQueue<GeometryData> dataQueue = new ConcurrentLinkedQueue<>();
    private ConcurrentLinkedQueue<String> vShaderQueue ;
    private ConcurrentLinkedQueue<String> fShaderQueue ;

    // Recorded on first call to draw, used to calculate future values of 'time' above.
    long systemStartTime = SystemClock.elapsedRealtime();

    public Geometry() {
        this.vertexShaderCode = DefaultShaders.BACKUP_VERTEX_SHADER;
        this.fragmentShaderCode = DefaultShaders.BACKUP_FRAGMENT_SHADER;
    }

    public Geometry(String vertexShaderCode, String fragmentShaderCode) {
        this.vertexShaderCode = vertexShaderCode;
        this.fragmentShaderCode = fragmentShaderCode;
    }


    @Override
    public void initGlProgram() throws ShaderCompileException {
        // NOTE: vertexShaderCode and fragmentShaderCode are initialized at construction.
        // NOTE: Each attribute is a String that holds a GLSL script.
        // NOTE: If changed between frames, make sure to call initGlProgram() again.

        // Let's jump into the loader code to see how this works...
        this.programHandle = SimpleGLUtils.loadGLProgram(vertexShaderCode, fragmentShaderCode);
    }

    @Override
    public void initBuffers() {
        // Vertices
        vertexCount = DefaultModels.CUBE_COORDS.length / 3;
        vertices = SimpleGLUtils.createFloatBuffer(DefaultModels.CUBE_COORDS);
        normals = SimpleGLUtils.createFloatBuffer(DefaultModels.CUBE_NORMALS);

        instancedCount = 1;
        translations = SimpleGLUtils.createFloatBuffer(new float[]{0f, 0f, 0f});
        rotations = SimpleGLUtils.createFloatBuffer(new float[]{0f, 0f, 0f});
        scales = SimpleGLUtils.createFloatBuffer(new float[]{1f, 1f, 1f});
        parameters = SimpleGLUtils.createFloatBuffer(new float[]{0f, 0f, 0f, 0f});

        colors = SimpleGLUtils.createFloatBuffer(new float[]{1f, 0f, 1f, 1f});
    }


    /**
     * TODO: Programs should be managed separately from models.
     *
     * @param programHandle
     */
    public void assignGLProgram(int programHandle) {
        this.programHandle = programHandle;
    }

    // NOTE: Handles are basically pointers to variables and constants that have been defined in our Shaders.
    @Override
    public void initHandles() {

        // program uniform handles (equivalent to global constants) 
        uMVMatrixHandle = GLES30.glGetUniformLocation(programHandle, "uMVMatrix");
        uMVPMatrixHandle = GLES30.glGetUniformLocation(programHandle, "uMVPMatrix");

        // program attribute handles (attributes are per-vertex parameters, as briefly explained earlier) 
        aPositionHandle = GLES30.glGetAttribLocation(programHandle, "aPosition");
        aNormalHandle = GLES30.glGetAttribLocation(programHandle, "aNormal");

        SimpleGLUtils.checkGlErrorCE("Error fetching 'Standard' Program handles");

        // NOTE: Per-*instance* data, see draw()
        aTranslationHandle = GLES30.glGetAttribLocation(programHandle, "aTranslationHandle");
        aRotationHandle = GLES30.glGetAttribLocation(programHandle, "aRotationHandle");
        aScaleHandle = GLES30.glGetAttribLocation(programHandle, "aScaleHandle");
        // TODO: Implement. Likely will want them 'per-instance' as well..
        aColorHandle = GLES30.glGetAttribLocation(programHandle, "aColor");

        aParametersHandle = GLES30.glGetAttribLocation(programHandle, "aParametersHandle");

        aTextureCoordinateHandle = GLES30.glGetAttribLocation(programHandle, "aTexCoordinate");

        SimpleGLUtils.checkGlErrorCE("Error fetching 'extra' Program handles");

        // **** Specialized / RayMarching handles
        uResolution = GLES30.glGetUniformLocation(programHandle, "uResolution");
        uEyeViewMatrix = GLES30.glGetUniformLocation(programHandle, "uEyeView");
        uLightPosHandle = GLES30.glGetUniformLocation(programHandle, "uLightPos");
        uTime = GLES30.glGetUniformLocation(programHandle, "uTime");

        uMode = GLES30.glGetUniformLocation(programHandle, "uMode");

        for( int i = 0 ; i < textureUnits.length ; i++ ) {
            uTextureUniformHandles[i] = GLES30.glGetUniformLocation(programHandle, "uTexture"+i);
        }

        SimpleGLUtils.checkGlErrorCE("Error fetching Raymarching Program handles");

        // NOTE: Depending how Geometry lifecycle ends up, we might need to disable these when removing geometry from a scene.
        // Finish up by enabling attrib arrays found in program.
        if (aPositionHandle != -1) GLES30.glEnableVertexAttribArray(aPositionHandle);
        if (aNormalHandle != -1) GLES30.glEnableVertexAttribArray(aNormalHandle);

        if (aTranslationHandle != -1) GLES30.glEnableVertexAttribArray(aTranslationHandle);
        if (aRotationHandle != -1) GLES30.glEnableVertexAttribArray(aRotationHandle);
        if (aScaleHandle != -1) GLES30.glEnableVertexAttribArray(aScaleHandle);
        if (aColorHandle != -1) GLES30.glEnableVertexAttribArray(aColorHandle);
        if (aParametersHandle != -1) GLES30.glEnableVertexAttribArray(aParametersHandle);

//        GLES30.glEnableVertexAttribArray(aTextureCoordinateHandle);

        SimpleGLUtils.checkGlErrorCE("Error enabling attrib arrays.");

    }

    int updateCount;

    /**
     * Should be called before each 'draw()'
     */
    @Override
    public void update(float[] perspectiveMatrix, float[] viewMatrix) {

        // Once every X updates.
        // TODO: Come up with something better re: update cycle.
        if ((updateCount++ % 60) == 0) updateGeometryData();

        updateTime();

        // Object first appears directly in front of user.
        Matrix.setIdentityM(modelMatrix, 0);
        Matrix.translateM(modelMatrix, 0, modelTranslation[0], modelTranslation[1], modelTranslation[2]);
        // TODO: Probably doing this wrong, fix this.
        Matrix.rotateM(modelMatrix, 0, modelRotation[0], 1.0f, 0.0f, 0.0f);
        Matrix.rotateM(modelMatrix, 0, modelRotation[1], 0.0f, 1.0f, 0.0f);
        Matrix.rotateM(modelMatrix, 0, modelRotation[2], 0.0f, 0.0f, 1.0f);
        Matrix.scaleM(modelMatrix,0,modelScale,modelScale,modelScale);

        Matrix.multiplyMM(modelViewMatrix4fv, 0, viewMatrix, 0, modelMatrix, 0);
        Matrix.multiplyMM(modelViewProjectionMatrix4fv, 0, perspectiveMatrix, 0, modelViewMatrix4fv, 0);
//        Matrix.multiplyMM(modelViewProjectionMatrix4fv, 0, perspectiveMatrix, 0, modelMatrix, 0);

        // Ready for draw() call.
    }

    /**
     * Called (at intervals) from the main loop.
     */
    private void updateGeometryData() {
        // Create new buffers for new geometry data.
        final GeometryData data = dataQueue.poll();
        if( data != null ) {
            final GeometryData.Obj obj = data.objs.get(0);
            updateBuffers(obj);
        }
        // New buffers are done, they'll be fed into OpenGL at the next draw() call.
    }

    private void updateBuffers(GeometryData.Obj obj) {
        vertexCount = obj.v.length/3; // NOTE: Should always be == to n.length/3, but not enforcing for now.
        vertices = SimpleGLUtils.createFloatBuffer(obj.v);
        normals = SimpleGLUtils.createFloatBuffer(obj.n);

        instancedCount = 1; // There's always at least one thing.
        translations = null; // Instance vars are optional.
        rotations = null;
        scales = null;
        colors = null;
        parameters = null;

        if (obj.i != null) {
            instancedCount = obj.i.instancedCount;
            if (obj.i.t != null) translations = SimpleGLUtils.createFloatBuffer(obj.i.t);
            if (obj.i.r != null) rotations = SimpleGLUtils.createFloatBuffer(obj.i.r);
            if (obj.i.s != null) scales = SimpleGLUtils.createFloatBuffer(obj.i.s);
            if (obj.i.c != null) colors = SimpleGLUtils.createFloatBuffer(obj.i.c);
            if (obj.i.p != null) parameters = SimpleGLUtils.createFloatBuffer(obj.i.p);
        }
    }

    private void updateTime() {
        // Our time reference.
        if (time1f == -1) {
            time1f = 0;
            systemStartTime = SystemClock.elapsedRealtime();
        } else {
            time1f = (SystemClock.elapsedRealtime() - systemStartTime) / 1000.0f;
        }
    }

    public void simpleDraw() {
        GLES30.glUseProgram(programHandle);

        // These uniforms are used in the vertex shader to plac...
        GLES30.glUniformMatrix4fv(uMVMatrixHandle, 1, false, modelViewMatrix4fv, 0);
        GLES30.glUniformMatrix4fv(uMVPMatrixHandle, 1, false, modelViewProjectionMatrix4fv, 0);

        // NOTE: Here we assign vertex byte buffer data to attributes.
        GLES30.glVertexAttribPointer(aPositionHandle, 3, GLES30.GL_FLOAT, false, 0, vertices);
        GLES30.glVertexAttribPointer(aNormalHandle, 3, GLES30.GL_FLOAT, false, 0, normals);

        GLES30.glDrawArrays(GLES30.GL_TRIANGLES, 0, 3);
    }

    @Override
    public void draw() {

        SimpleGLUtils.checkGlErrorRTE("beginning of draw"); // Extra check, this spot is sensitive.
        GLES30.glUseProgram(programHandle);
        SimpleGLUtils.checkGlErrorRTE("Error on glUseProgram()"); // Extra check, this spot is sensitive.

        // Model/View/Projection Matrix assignments
        // TODO: Add the model as well, to help with lighting.
        GLES30.glUniformMatrix4fv(uMVMatrixHandle, 1, false, modelViewMatrix4fv, 0);
        SimpleGLUtils.checkGlErrorRTE("Error on uMVMatrixHandle"); // Extra check, this spot is sensitive.
        GLES30.glUniformMatrix4fv(uMVPMatrixHandle, 1, false, modelViewProjectionMatrix4fv, 0); // Apply the projection and view transformation
        SimpleGLUtils.checkGlErrorRTE("Error on uMVPMatrixHandle"); // Extra check, this spot is sensitive.

        // Texture assignments
        for( int i = 0 ; i < textureDataHandles.length && i < textureUnits.length ; i++ ) {
            // Set the active texture unit to texture unit [i].
            GLES30.glActiveTexture(textureUnits[i]);
            // Bind the texture to this unit.
            GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, textureDataHandles[i]);
            // Tell the texture uniform sampler to use this texture in the shader by binding to texture unit [i].
            GLES30.glUniform1i(uTextureUniformHandles[i], i);
        }
        SimpleGLUtils.checkGlErrorCE("Error assigning / binding textures");

        // More 'Fragment shader specific' assignments
        GLES30.glUniform1i(uMode, mode1i);
        GLES30.glUniformMatrix4fv(uEyeViewMatrix, 1, false, eyeViewMatrix4fv, 0); // Allows shader to situate itself.
        GLES30.glUniform1f(uTime, time1f);

        GLES30.glUniform2fv(uResolution, 1, resolution2fv, 0);
        GLES30.glUniform3fv(uLightPosHandle, 1, lightPos3fv, 0);

        SimpleGLUtils.checkGlErrorCE("Error assigning Raymarching Program values");

        // Assign vertex data to attrib.
        GLES30.glVertexAttribPointer(aPositionHandle, 3, GLES30.GL_FLOAT, false, 0, vertices);
        GLES30.glVertexAttribPointer(aNormalHandle, 3, GLES30.GL_FLOAT, false, 0, normals);

        if (aTranslationHandle != -1 && translations != null) {
            GLES30.glVertexAttribPointer(aTranslationHandle, 3, GLES30.GL_FLOAT, false, 0, translations);
            GLES30.glVertexAttribDivisor(aTranslationHandle, 1);
        }
        if (aRotationHandle != -1 && rotations != null) {
            GLES30.glVertexAttribPointer(aRotationHandle, 3, GLES30.GL_FLOAT, false, 0, rotations);
            GLES30.glVertexAttribDivisor(aRotationHandle, 1);
        }
        if (aScaleHandle != -1 && scales != null) {
            GLES30.glVertexAttribPointer(aScaleHandle, 3, GLES30.GL_FLOAT, false, 0, scales);
            GLES30.glVertexAttribDivisor(aScaleHandle, 1);
        }
        if( aColorHandle != -1 && colors != null) {
            GLES30.glVertexAttribPointer(aColorHandle, 4, GLES30.GL_FLOAT, false, 0, colors);
            GLES30.glVertexAttribDivisor(aColorHandle, 1);
        }
        if (aParametersHandle != -1 && parameters != null) {
            GLES30.glVertexAttribPointer(aParametersHandle, 4, GLES30.GL_FLOAT, false, 0, parameters);
            GLES30.glVertexAttribDivisor(aParametersHandle, 1);
        }


        // Draw command
        // https://developer.apple.com/library/ios/documentation/3DDrawing/Conceptual/OpenGLES_ProgrammingGuide/Performance/Performance.html#//apple_ref/doc/uid/TP40008793-CH105-SW21
        GLES30.glDrawArraysInstanced(GLES30.GL_TRIANGLES, 0, vertexCount, instancedCount);
        SimpleGLUtils.checkGlErrorCE("Drawing cube");

/*
        // Finish up by enabling attrib arrays
        GLES30.glDisableVertexAttribArray(aPositionHandle);
        GLES30.glDisableVertexAttribArray(aNormalHandle);
//        GLES30.glDisableVertexAttribArray(aColorHandle);
        GLES30.glDisableVertexAttribArray(aTextureCoordinateHandle);
*/

    }

    /**
     * Enqueue only if there's a spot for it.
     *
     * @param geometryData the received data.
     * @return if addition was successful or not.
     */
    public boolean queue(GeometryData geometryData) {
        return dataQueue.isEmpty() && dataQueue.add(geometryData);
    }

    public void setTranslation(float x, float y, float z) {
        this.modelTranslation[0] = x;
        this.modelTranslation[1] = y;
        this.modelTranslation[2] = z;
    }

    public void setRotation(float x, float y, float z) {
        this.modelRotation[0] = x;
        this.modelRotation[1] = y;
        this.modelRotation[2] = z;
    }

    public void setScale(float s) {
        this.modelScale = s;
    }

    public void setResolution2fv(float width, float height) {
        this.resolution2fv[0] = width;
        this.resolution2fv[1] = height;
    }

    public void setEyeViewMatrix4fv(float[] eyeViewMatrix4fv) {
        for (int i = 0; i < eyeViewMatrix4fv.length; i++) this.eyeViewMatrix4fv[i] = eyeViewMatrix4fv[i];
    }

    public void setTextureDataHandles(int[] textureDataHandles) {
        this.textureDataHandles = textureDataHandles;
    }

    public void setMode1i(int mode1i) {
        this.mode1i = mode1i;
    }

    public void setLightPos3fv(float[] lightPos3fv) {
        for (int i = 0; i < lightPos3fv.length; i++) this.lightPos3fv[i] = lightPos3fv[i];
    }

    // NOTE: If changed on the fly, initGLProgram() needs to be called.
    public void setVertexShaderCode(String vertexShaderCode) {
        this.vertexShaderCode = vertexShaderCode;
    }

    // NOTE: If changed on the fly, initGLProgram() needs to be called.
    public void setFragmentShaderCode(String fragmentShaderCode) {
        this.fragmentShaderCode = fragmentShaderCode;
    }

    public void setTime1f(float time1f) {
        this.time1f = time1f;
    }
}
