package com.kanawish.shaderlib.gl;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLES31;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.os.SystemClock;

import com.google.vrtoolkit.cardboard.Eye;
import com.kanawish.shaderlib.R;
import com.kanawish.shaderlib.domain.CameraManager;
import com.kanawish.shaderlib.domain.DebugData;
import com.kanawish.shaderlib.model.GeometryData;
import com.kanawish.shaderlib.model.GeometryRenderer;
import com.kanawish.shaderlib.utils.IOUtils;
import com.kanawish.shaderlib.utils.ShaderCompileException;
import com.kanawish.shaderlib.utils.SimpleGLUtils;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import rx.Observable;
import rx.Subscription;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.observables.MathObservable;
import rx.subjects.PublishSubject;
import timber.log.Timber;

/**
 * Created by ecaron on 15-11-24.
 */
public class DebugGLRenderer implements GLSurfaceView.Renderer {

    private final Context context;
    private final GeometryRenderer geometry;

    // SECTION: Camera Control
    // GOAL: Provide with a way to navigate the space with regular controls
    private CameraManager cameraManager;
    private float[] cameraRotation = new float[] {0,0,0};
    private float[] cameraTranslation = new float[] {0,0,0};


    int frameCount = 0 ;

    private PublishSubject<Float> fpsSubject;
    private Subscription movingAverageSubscription;

    private boolean compileError = false;

    private long benchLastTime = 0;
    private long benchCurrentTime = 0;
    private float benchMs = 0;

    private final float[] camera = new float[16];
    private final float[] pMatrix = new float[16];

    private int width;
    private int height;

    private float ratio;

    private DebugData debugData;


    public DebugGLRenderer(Context context) {
        this.context = context;

        // Load initial shaders.
        String instancedVertexShader = null;
        String instancedFragmentShader = null;

        try {
            instancedVertexShader = IOUtils.loadStringFromAsset(context, "shaders/_300.instanced.v4.vs");
            instancedFragmentShader = IOUtils.loadStringFromAsset(context, "shaders/_300.default.v4.fs");
        } catch (IOException e) {
            Timber.e(e,"Failed to load shaders from disk.");
            throw new RuntimeException(e);
        }

        geometry = new GeometryRenderer(instancedVertexShader,instancedFragmentShader);

        // TODO: Implement
//        this.cameraManager = cameraManager;
//        initCameraManager(cameraManager);

        // TODO: Implement
//        initDebugDataPublisher();

    }

    private void initDebugDataPublisher() {
        // Observable event streams for debug info (Headtracking)
        // TODO: DebugData is really a rough way to go. Emit Coordinate objects eventually instead?
//        debugOutputPublishSubject = PublishSubject.create();
        debugData = new DebugData(); // Default to RIGHT

        // Regular calls to benchmarkFps() will publish the info we need at the next step.
        fpsSubject = PublishSubject.create();

        // We use fpsSubject as a source, and average it over the last 50 results.
        Observable<Float> movingAverage = movingAverage(fpsSubject.asObservable(), 50);
        movingAverageSubscription = movingAverage
                .sample(500, TimeUnit.MILLISECONDS)
                .subscribe(new Action1<Float>() {
                    @Override
                    public void call(Float average) {
                        debugData.setFps(average);
                    }
                });
    }

    private void benchmarkFps() {
        benchCurrentTime = SystemClock.elapsedRealtime();
        benchMs = (float) ( (benchCurrentTime - benchLastTime) );
        benchLastTime = benchCurrentTime;
        fpsSubject.onNext(benchMs);
    }

    public static Observable<Float> movingAverage(Observable<Float> o, int N) {
        return o.window(N, 1).flatMap(
                new Func1<Observable<Float>, Observable<Float>>() {
                    public Observable<Float> call(Observable<Float> window) {
                        return MathObservable.averageFloat(window);
                    }
                }
        );
    }

    private void initCameraManager(CameraManager cameraManager) {
        cameraManager
            .cameraDataObservable()
            .subscribe(
                new Action1<CameraManager.CameraData>() {
                    @Override
                    public void call(CameraManager.CameraData cameraData) {
                        // Copy the arrays.
                        float[] newRot = cameraData.getCameraRotation();
                        System.arraycopy(newRot, 0, cameraRotation, 0, newRot.length);
                        float[] newTrans = cameraData.getCameraTranslation();
                        System.arraycopy(newTrans, 0, cameraTranslation, 0, newTrans.length);
                    }
                });
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        GLES31.glClearColor(0f,0f,0f,0f);

        Timber.i("onSurfaceCreated()");

        // Initialize some default textures, much like Shadertoy's approach.
        int[] textureDataHandles = {
                SimpleGLUtils.loadTexture(context, R.drawable.tex03),
                SimpleGLUtils.loadTexture(context, R.drawable.tex12),
                SimpleGLUtils.loadTexture(context, R.drawable.tex16),
        };

        // To keep the sample code organized, and future proof it a bit for when we'll want to
        // construct more complex scenes, I've created a custom class called "Geometry".

        // 1.- Buffers are data structures we use to pass data into the pipeline. 
        geometry.initBuffers();
        // 2.- We initialize the OpenGL program.
        try {
            geometry.initGlProgram();
        } catch (ShaderCompileException e) {
            // We need at least the starting shaders to compile, otherwise we'll fail fast.
            throw new RuntimeException(e);
        }
        // 3.- Once the program is ready, we can start initializing handles...
        geometry.initHandles();

        geometry.setTextureDataHandles(textureDataHandles);

        SimpleGLUtils.checkGlErrorRTE("Geometry initialized");

        // OpenGL Pipeline configuration
        GLES31.glEnable(GLES31.GL_DEPTH_TEST);

    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        this.width = width ;
        this.height = width ;
        GLES31.glViewport(0,0,width,height);
        // Get a perspective ratio for the eye view.
        ratio = (float) width / height;
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        GLES31.glClear(GLES31.GL_COLOR_BUFFER_BIT);
        // TODO: Draw calls
        onNewFrame();
        onDrawEye();

    }

    private void onNewFrame() {
        // TODO: Re-build fps feed.
        // Updates this.benchFps so we can report on it. See onDrawEye()
//        benchmarkFps();

        SimpleGLUtils.checkGlErrorCE("onReadyToDraw");

        // Set the camera position (aka View matrix)
        Matrix.setLookAtM(camera, 0,
                0, 0, 1f, // CAMERA_Z, // used to be 4, // eye xyz
                0f, 0f, 0f, // center xyz
                0f, 1.0f, 0.0f); // up vector xyz

        // Here we can set general frame attributes.

        // TODO: Make it dynamic.
        geometry.setLightPos3fv(new float[]{0, 20, -3});

    }

    float[] vMatrix = new float[16];

    public void onDrawEye() {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        // Setup pMatrix, our 'projection matrix'
        // left & right, bottom & top, near & far
        Matrix.frustumM(pMatrix, 0, -ratio, ratio, -1, 1, 3, 200);

        // Calculate the projection and view transformation
        // TODO: Likely going to be a good idea to review the camera / model projection ops here.
//        Matrix.multiplyMM(vMatrix, 0, , 0, camera, 0);

        // Temporarily disabling the touch-camera movement,
//        Matrix.rotateM(pMatrix, 0, cameraRotation[1] * 0.05f, 1.0f, 0.0f, 0.0f);
//        Matrix.rotateM(pMatrix, 0, cameraRotation[0] * 0.05f, 0.0f, 1.0f, 0.0f);
//        Matrix.translateM(pMatrix, 0, cameraTranslation[0], cameraTranslation[1], cameraTranslation[2]);

        geometry.setResolution2fv(width, height);
        geometry.setEyeViewMatrix4fv(camera); // ... hmm

        // TODO: Re-enable light-pos and time?
//        geometry.setLightPos3fv();
//        geometry.setTime1f();

        geometry.update(pMatrix, camera);
        geometry.draw();
    }

    // TODO: Take 3 'update' methods and use composition, aiming to share code with Stereo Renderer.
    /**
     * Live update the geometry data, queue the change.
     * @param newGeometryData
     * @return if data was queued or not.
     */
    public boolean updateGeometryData(GeometryData newGeometryData) {
        return geometry.queue(newGeometryData);
    }

    /**
     * Live updates the shader, if compile fails, stays with old version.
     * <p/>
     * Need to make sure we run this in the right thread.
     */
    public void updateVertexShader(String newVertexShaderCode) {
        compileError = false ;
        try {
            geometry.setVertexShaderCode(newVertexShaderCode);
            geometry.initGlProgram();
        } catch (ShaderCompileException e) {
            Timber.d("Couldn't compile shader, will stay with previous version.", e);
            compileError = true ;
            return;
        }
        geometry.initHandles();
    }

    /**
     * Live updates the fragment shader, if compile fails, stays with old version.
     * <p/>
     * Need to make sure we run this in the right thread.
     */
    public void updateFragmentShader(String newFragmentShaderCode) {
        compileError = false ;
        try {
            geometry.setFragmentShaderCode(newFragmentShaderCode);
            geometry.initGlProgram();
        } catch (ShaderCompileException e) {
            Timber.d("Couldn't compile shader, will stay with previous version.", e);
            compileError = true ;
            return;
        }
        geometry.initHandles();
    }

}
