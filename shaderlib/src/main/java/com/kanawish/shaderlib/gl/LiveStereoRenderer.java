/*
 * Copyright 2014 Google Inc. All Rights Reserved.

 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.kanawish.shaderlib.gl;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.Matrix;
import android.os.SystemClock;

import com.google.vrtoolkit.cardboard.CardboardView;
import com.google.vrtoolkit.cardboard.Eye;
import com.google.vrtoolkit.cardboard.HeadTransform;
import com.google.vrtoolkit.cardboard.Viewport;
import com.kanawish.shaderlib.R;
import com.kanawish.shaderlib.domain.CameraManager;
import com.kanawish.shaderlib.domain.DebugData;
import com.kanawish.shaderlib.model.Geometry;
import com.kanawish.shaderlib.model.GeometryData;
import com.kanawish.shaderlib.utils.IOUtils;
import com.kanawish.shaderlib.utils.ShaderCompileException;
import com.kanawish.shaderlib.utils.SimpleGLUtils;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import javax.microedition.khronos.egl.EGLConfig;

import rx.Observable;
import rx.Subscription;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.observables.MathObservable;
import rx.subjects.PublishSubject;
import timber.log.Timber;

/**
 *
 * Adapted from the Cardboard sample application, this class is designed for
 * rapid fragment shader development.
 * <p/>
 * Aiming for a set of base features that allow to build a Cardboard VR Shadertoy-like platform,
 * but we also want for users of the library to be able to adapt this to build full fledged VR apps.
 *
 */
public class LiveStereoRenderer implements CardboardView.StereoRenderer {

    // BENCHMARKS / DEBUG
    private final Subscription movingAverageSubscription;
    private final PublishSubject<DebugData> debugOutputPublishSubject;
    private final DebugData debugDataRight;
    private final DebugData debugDataLeft;
    private boolean compileError = false;

    private long benchLastTime = 0;
    private long benchCurrentTime = 0;
    private float benchMs = 0;


    // SECTION: Android specific / resources
    private final Context context; // Mostly needed to load textures. See about moving this in a manager.


    // TODO: Re-implement a simplified version of original scheme.
    // SECTION: Playback Control
    enum State { PLAYING, PAUSED, STOPPED }
    private State state;
    private long currentTime = 0; // sequence 'read-head' position
    private long lastClock; // last clock recorded, on "play()" or at last frame render when "PLAYING".


    // SECTION: Camera Control
    // GOAL: Provide with a way to navigate the space with regular controls
    private final CameraManager cameraManager;
    private float[] cameraRotation = new float[] {0,0,0};
    private float[] cameraTranslation = new float[] {0,0,0};


    // SECTION: 3d Model Entities
    private Geometry geometry;


    // SECTION: GL States
    private static final float Z_NEAR = 3f;//0.1f;
    private static final float Z_FAR = 7f;//100.0f;

    private static final float CAMERA_Z = 0.01f;

    private final float[] scratch1 = new float[16];
    private final float[] forward = new float[3];

    private final float[] resolution = new float[2];

    private float[] headView = new float[16];
    private final float[] camera = new float[16];

    private final float[] pMatrix = new float[16];
    private final float[] mvMatrix = new float[16];
    private final float[] mvpMatrix = new float[16];



    public LiveStereoRenderer(Context context, CameraManager cameraManager) {

        // TODO Use dagger, make things cleaner / easier.
        this.context = context;

        this.cameraManager = cameraManager;
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

        // Load initial shaders.
        String instancedVertexShader = null;
        String instancedFragmentShader = null;

        try {
            instancedVertexShader = IOUtils.loadStringFromAsset(context, "shaders/_300.instanced.v1.vs");
            instancedFragmentShader = IOUtils.loadStringFromAsset(context, "shaders/_300.default.v1.fs");
        } catch (IOException e) {
            Timber.e(e,"Failed to load shaders from disk.");
            throw new RuntimeException(e);
        }

        geometry = new Geometry(instancedVertexShader,instancedFragmentShader);

/*
        int range = 2;
        for (int i = 0; i < range; i++)
            for (int j = 0; j < range; j++)
                for (int k = 0; k < range; k++) {
                    final Cube model = new Cube();
                    model.setScale(0.25f);
                    model.setTranslation(i-1,j-1,-k-2);
//                    final float[] color4fv = BLUE ; // {i/10f,j/10f,k/10f,1f};
                    float div = (float)range;
                    final float[] color4fv = {1f-i/div,j/div,k/div,1f};
                    model.setColor4fv(color4fv); // TODO: Optim
                    models.add(model);
                }
*/

        // Observable event streams for debug info (Headtracking)
        // TODO: DebugData is really a rough way to go. Emit Coordinate objects eventually instead?
        debugOutputPublishSubject = PublishSubject.create();
        debugDataRight = new DebugData(); // Default to RIGHT
        debugDataLeft = new DebugData();
        debugDataLeft.setType(DebugData.Type.LEFT);

        // This averages FPS over the last 50 results.
        Observable<Float> movingAverage = movingAverage(fpsSubject.asObservable(), 50);
        movingAverageSubscription = movingAverage
            .sample(500, TimeUnit.MILLISECONDS)
            .subscribe(new Action1<Float>() {
                @Override
                public void call(Float average) {
                    debugDataRight.setFps(average);
                    debugDataLeft.setFps(average);
                }
            });

    }

    /**
     * Creates the buffers we use to store information about the 3D world.
     * <p/>
     * <p>OpenGL doesn't use Java arrays, but rather needs data in a format it can understand.
     * Hence ByteBuffers.
     *
     * @param config The EGL configuration used when creating the surface.
     */
    @Override
    public void onSurfaceCreated(EGLConfig config) {
        Timber.i("onSurfaceCreated");
        GLES20.glClearColor(0.1f, 0.1f, 0.9f, 1.0f); // Dark background so text shows up well.

        // Initialize some default textures, inspired by Shadertoy's approach.
        int[] textureDataHandles = {
            SimpleGLUtils.loadTexture(context, R.drawable.tex03),
            SimpleGLUtils.loadTexture(context, R.drawable.tex12),
            SimpleGLUtils.loadTexture(context, R.drawable.tex16),
        };

        // Load initial shaders.
/*
        String vertexShaderCode = null;
        String fragmentShaderCode = null;
        try {
            vertexShaderCode = IOUtils.loadStringFromAsset(context, "shaders/default.vs");
            fragmentShaderCode = IOUtils.loadStringFromAsset(context, "shaders/default.fs");
//            fragmentShaderCode = IOUtils.loadStringFromAsset(context, "shaders/geo_01c_pyramid_fields.fs");
        } catch (IOException e) {
            Timber.e(e,"Failed to load shaders from disk, using backup shaders.");
            vertexShaderCode = DefaultShaders.BACKUP_VERTEX_SHADER;
            fragmentShaderCode = DefaultShaders.BACKUP_FRAGMENT_SHADER;
        }

        try {
            modelsProgramHandle = SimpleGLUtils.loadGLProgram(vertexShaderCode,fragmentShaderCode);
        } catch (ShaderCompileException e) {
            Timber.e(e,"Failed to loadGLProgram()");
            throw new RuntimeException(e);
        }
*/

        // SHADER RECTANGLE
/*
        try {
            shaderRectangle.initGlProgram();
        } catch (ShaderCompileException e) {
            // We need at least the starting shaders to compile, otherwise we'll fail fast.
            throw new RuntimeException(e);
        }
        shaderRectangle.initBuffers();
        shaderRectangle.initHandles();

        shaderRectangle.setTextureDataHandles(textureDataHandles);
*/

        // GEOMETRY
        try {
            geometry.initGlProgram();
        } catch (ShaderCompileException e) {
            // We need at least the starting shaders to compile, otherwise we'll fail fast.
            throw new RuntimeException(e);
        }
        geometry.initBuffers();
        geometry.initHandles();
        geometry.setTextureDataHandles(textureDataHandles);

/*
        for (Cube current:models) {
            current.assignGLProgram(modelsProgramHandle);
            current.initBuffers();
            current.initHandles();
            current.setTextureDataHandles(textureDataHandles);
        }
*/

        SimpleGLUtils.checkGlErrorRTE("scenes initialized");
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);

    }

    @Override
    public void onSurfaceChanged(int width, int height) {
        Timber.i("onSurfaceChanged");

        // In a non-stereo renderer, we typically would assign resolution to various items.
    }

    /**
     * Prepares OpenGL ES before we draw a frame.
     *
     * @param headTransform The head transformation in the new frame.
     */
    @Override
    public void onNewFrame(HeadTransform headTransform) {
        // Updates this.benchFps so we can report on it. See onDrawEye()
        benchmarkFps();

        SimpleGLUtils.checkGlErrorCE("onReadyToDraw");

        // Set the camera position (View matrix)
//        float[] startPos = new float[16];
        Matrix.setLookAtM(camera, 0,
            0, 0, 5.5f, // CAMERA_Z, // used to be 4, // eye xyz
            0f, 0f, 0f, // center xyz
            0f, 1.0f, 0.0f); // up vector xyz

        // TODO: Test
//        float[] rotMatrix = new float[16];
//        Matrix.rotateM(rotMatrix, 0, cameraRotation[2], 0.0f, 0.0f, 1.0f);

//        Matrix.multiplyMM(camera, 0, rotMatrix, 0, startPos, 0);

        headTransform.getHeadView(headView, 0);

        // Here we can set all the frame/scene-level variables that are relevant

        // TODO: Make it dynamic.
        geometry.setLightPos3fv(new float[] {0,20,-3});

/*
        float[] fwd = shaderRectangle.getForwardVec3();
        headTransform.getForwardVector(fwd, 0);
        float[] up = shaderRectangle.getUpVec3();
        headTransform.getUpVector(up, 0);
        float[] right = shaderRectangle.getRightVec3();
        headTransform.getRightVector(right, 0);
*/

    }

    int frameCount = 0 ;
    /**
     * Draws a frame for an eye.
     *
     * @param eye The eye to render. Includes all required transformations.
     */
    @Override
    public void onDrawEye(Eye eye) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        SimpleGLUtils.checkGlErrorCE("colorParam");

        // Orthogonal-ish projection for raymarching trick.
        float ratio = (float) eye.getViewport().width / eye.getViewport().height;

        // Quick patch for backpressure problem.
        if( frameCount++ % 15 == 0 ) generateDebugOutput(eye);

        // left & right, bottom & top, near & far
        Matrix.frustumM(pMatrix, 0, -ratio, ratio, -1, 1, 3, 70);

        // Calculate the projection and view transformation
        float[] view = new float[16];
        // TODO: Likely going to be a good idea to review the camera / model projection ops here.
        if(true) {
            Matrix.multiplyMM(view, 0, eye.getEyeView(), 0, camera, 0);

            Matrix.rotateM(pMatrix, 0, cameraRotation[1] * 0.05f, 1.0f, 0.0f, 0.0f);
            Matrix.rotateM(pMatrix, 0, cameraRotation[0] * 0.05f, 0.0f, 1.0f, 0.0f);
            Matrix.translateM(pMatrix, 0, cameraTranslation[0], cameraTranslation[1], cameraTranslation[2]);

            Matrix.multiplyMM(mvpMatrix, 0, pMatrix, 0, view, 0); // We apply the eye view in this case.
        } else {
            view = camera ;
            Matrix.multiplyMM(mvpMatrix, 0, pMatrix, 0, camera, 0); // We ignore the eye view here.
        }

/*
        shaderRectangle.setEyeView(eye.getEyeView());
        shaderRectangle.setResolution(eye.getViewport().width, eye.getViewport().height);
        float[] color = eye.getType() == Eye.Type.MONOCULAR ? BLUE : eye.getType() == Eye.Type.LEFT ? RED : GREEN;
        shaderRectangle.setMode(1);
        shaderRectangle.setColorVec4(color);
*/

        // This has been failing when used alongside geometry draw().
        // TODO Investigate possible causes, see below.
        // http://stackoverflow.com/questions/12017175/what-can-cause-gldrawarrays-to-generate-a-gl-invalid-operation-error
//        shaderRectangle.draw(mvpMatrix);

        geometry.setResolution2fv(eye.getViewport().width, eye.getViewport().height);
//        geometry.setScale(0.25f);
        geometry.update(pMatrix, view);
        geometry.draw();

/*
        for (Cube current : models) {
//        for( int i = 0 ; i < 2 ; i++) {
//            Geometry current = models.get(i);
            current.setResolution2fv(eye.getViewport().width, eye.getViewport().height);
            current.update(pMatrix, view);
            current.draw();
        }
*/
        SimpleGLUtils.checkGlErrorCE("Drawing cube");

    }

    @Override
    public void onFinishFrame(Viewport viewport) {
    }

    @Override
    public void onRendererShutdown() {
        Timber.i("onRendererShutdown");
        debugOutputPublishSubject.onCompleted();
        fpsSubject.onCompleted();
        movingAverageSubscription.unsubscribe();
    }

    // TODO: Quick and dirty delegation to test/debug.

    /**
     * Live update the geometry data, queue the change.
     * @param geometryData
     * @return if data was queued or not.
     */
    public boolean updateGeometryData(GeometryData geometryData) {
        return geometry.queue(geometryData);
    }

    /**
     * Live updates the shader, if compile fails, stays with old version.
     * <p/>
     * Need to make sure we run this in the right thread.
     */
    public void updateVertexShader(String code) {
        compileError = false ;
        try {
            geometry.setVertexShaderCode(code);
            geometry.initGlProgram();
//            shaderRectangle.setVertexShaderCode(code);
//            shaderRectangle.initGlProgram();
        } catch (ShaderCompileException e) {
            Timber.d("Couldn't compile shader, will stay with previous version.", e);
            compileError = true ;
            return;
        }
        geometry.initHandles();
//        shaderRectangle.initHandles();
    }

    /**
     * Live updates the fragment shader, if compile fails, stays with old version.
     * <p/>
     * Need to make sure we run this in the right thread.
     */
    public void updateFragmentShader(String fragmentShaderCode) {
        compileError = false ;
        try {
            geometry.setFragmentShaderCode(fragmentShaderCode);
            geometry.initGlProgram();
//            shaderRectangle.setFragmentShaderCode(fragmentShaderCode);
//            shaderRectangle.initGlProgram();
        } catch (ShaderCompileException e) {
            Timber.d("Couldn't compile shader, will stay with previous version.", e);
            compileError = true ;
            return;
        }
        geometry.initHandles();
//        shaderRectangle.initHandles();
    }

    /**
     * Subscribe to get debug data stream.
     *
     * @param onNext called with updates.
     * @return subscription, can be useful for lifecycle management.
     */
    public Subscription subscribe(Action1<? super DebugData> onNext) {
        return debugOutputPublishSubject.subscribe(onNext);
    }

    public PublishSubject<DebugData> getDebugOutputPublishSubject() {
        return debugOutputPublishSubject;
    }

    public void pause() {
        state = State.PAUSED;
    }

    public void play() {
        state = State.PLAYING;
        lastClock = SystemClock.uptimeMillis();
    }

    public void seekTo(long time) {
        currentTime = time;
    }

    private void generateDebugOutput(Eye eye) {
        float[] ev = eye.getEyeView();
        if (eye.getType() == Eye.Type.RIGHT) {
            debugDataRight.setLine1(String.format("%1.2f, %1.2f, %1.2f, %1.2f", ev[0], ev[1], ev[2], ev[3]));
            debugDataRight.setLine2(String.format("%1.2f, %1.2f, %1.2f, %1.2f", ev[4], ev[5], ev[6], ev[7]));
            debugDataRight.setLine3(String.format("%1.2f, %1.2f, %1.2f, %1.2f", ev[8], ev[9], ev[10], ev[11]));
            debugDataRight.setLine4(String.format("R %1.2f, %1.2f, %1.2f, %1.2f", ev[12], ev[13], ev[14], ev[15]));
//            debugDataRight.setFps(benchFps);
            debugDataRight.setCompileError(compileError);

            debugOutputPublishSubject.onNext(debugDataRight);
        } else {
            debugDataLeft.setLine1(String.format("%1.2f, %1.2f, %1.2f, %1.2f", ev[0], ev[1], ev[2], ev[3]));
            debugDataLeft.setLine2(String.format("%1.2f, %1.2f, %1.2f, %1.2f", ev[4], ev[5], ev[6], ev[7]));
            debugDataLeft.setLine3(String.format("%1.2f, %1.2f, %1.2f, %1.2f", ev[8], ev[9], ev[10], ev[11]));
            debugDataLeft.setLine4(String.format("%1.2f, %1.2f, %1.2f, %1.2f L", ev[12], ev[13], ev[14], ev[15]));
//            debugDataLeft.setFps(benchFps);
            debugDataLeft.setCompileError(compileError);

            debugOutputPublishSubject.onNext(debugDataLeft);
        }
    }

    PublishSubject<Float> fpsSubject = PublishSubject.create();

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
}
