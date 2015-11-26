package com.kanawish.androidvrtalk.ui;

import android.app.Activity;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.Handler;

import com.kanawish.androidvrtalk.domain.ScriptManager;
import com.kanawish.androidvrtalk.injection.Injector;
import com.kanawish.shaderlib.domain.CameraManager;
import com.kanawish.shaderlib.domain.GeometryManager;
import com.kanawish.shaderlib.domain.PipelineProgramBus;
import com.kanawish.shaderlib.generation.BasicGenerator;
import com.kanawish.shaderlib.generation.DefaultModels;
import com.kanawish.shaderlib.gl.DebugGLRenderer;
import com.kanawish.shaderlib.model.GeometryData;
import com.kanawish.shaderlib.utils.IOUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import dagger.ObjectGraph;
import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.HandlerScheduler;
import rx.functions.Action1;
import rx.schedulers.Schedulers;
import timber.log.Timber;

/**
 * Created by ecaron on 15-11-24.
 */
public class PlainGLActivity extends Activity {

    private ObjectGraph activityGraph;

    // When instantiated, will publish script changes.
    @Inject
    ScriptManager scriptManager;

    // The camera manager will be used to help us move the viewpoint in our scene, etc.
    @Inject
    CameraManager cameraManager;
    @Inject
    PipelineProgramBus programBus;
    @Inject
    GeometryManager geometryManager;
    @Inject
    BasicGenerator basicGenerator;

    private String geoWrapper;

    // Subscriptions to the code updaters
    private Subscription geoSub;
    private Subscription vertexSub;
    private Subscription fragSub;

    //    @Bind(R.id.debug_surface_view)
    GLSurfaceView debugGLSurfaceView;
    private DebugGLRenderer renderer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ObjectGraph appGraph = Injector.obtain(getApplication());
        appGraph.inject(this);

        // Not really used yet, but added this to demonstrate best practice.
        this.activityGraph = appGraph.plus(new ActivityModule(this));

        debugGLSurfaceView = new GLSurfaceView(this);
        debugGLSurfaceView.setEGLContextClientVersion(3);
        renderer = new DebugGLRenderer(this);
        debugGLSurfaceView.setRenderer(renderer);
        debugGLSurfaceView.setOnClickListener(v -> programBus.publishGeoData(basicGenerator.generateScene()));

        setContentView(debugGLSurfaceView);
//      ButterKnife.bind(this);

        // Load geoWrapper from local storage.
        try {
            geoWrapper = IOUtils.loadStringFromAsset(this, "js/wrapper.js");
        } catch (IOException e) {
            Timber.e(e, "Failed to load 'js/wrapper.js'");
            throw new RuntimeException("Critical failure, app is missing 'wrapper.js' asset.");
        }

        // Prime the pump, as it were. We want at least a default geometry in place.
        try {
            // NOTE: Pick your poison.
//            String geoJs = IOUtils.loadStringFromAsset(this, "js/bundle.js");
//            programBus.publishGeoScript(geoJs);
            programBus.publishGeoData(generateScene());

            String vertexShader = IOUtils.loadStringFromAsset(this, "shaders/_300.instanced.v5.vs");
            programBus.publishVertexShader(vertexShader);
            String fragmentShader = IOUtils.loadStringFromAsset(this, "shaders/_300.default.v5.fs");
            programBus.publishFragmentShader(fragmentShader);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public GeometryData generateScene() {
        Timber.d("generateScene()");
        Timber.d("buildCube()");
        GeometryData data = new GeometryData();
        data.objs = new ArrayList<>();
        float deg = (float) (Math.PI / 2f);
        GeometryData.Instanced i =
                new GeometryData.Instanced(1, new float[][]{{-0.0f, 0.0f, -15f}, {deg / 2, deg / 2, 0}, {1, 1, 1}, {1.0f, 0.0f, 1.0f, 1}, {1, 0, 0, 0}});
        GeometryData.Obj obj = new GeometryData.Obj(DefaultModels.CUBE_COORDS, DefaultModels.CUBE_NORMALS, i);
        data.objs.add(obj);
        return data;
//        return DefaultModels.buildCube();
    }

    @Override
    protected void onResume() {
        super.onResume();
        this.subscribeProgramBus();
    }

    @Override
    protected void onPause() {
        super.onPause();
        this.unsubscribeProgramBus();
    }

    public void subscribeProgramBus() {

/*
        geoSub = programBus
                .geoScriptBus()
                .debounce(500, TimeUnit.MILLISECONDS)
                .map(script -> String.format(geoWrapper, script)) // TODO: Came from original rhino setup, might be removeable
                .observeOn(Schedulers.computation())
                .map(geometryManager::webviewGeometryData)
                .doOnError(throwable -> Timber.e(throwable, "GeometryScript failed to execute."))
                .retryWhen(e -> e.flatMap( i -> Observable.timer(5000, TimeUnit.MILLISECONDS)))
                .subscribe(renderer::updateGeometryData);
*/

        geoSub = programBus
                .geoDataBus()
                .subscribe(new Action1<GeometryData>() {
                    @Override
                    public void call(GeometryData geometryData) {
                        renderer.updateGeometryData(geometryData);
                    }
                });
//                .subscribe(renderer::updateGeometryData);

        vertexSub = programBus
                .vertexShaderBus()
                .debounce(500, TimeUnit.MILLISECONDS)
                .doOnNext(shader -> Timber.d("Vector Shader code changed."))
                .observeOn(HandlerScheduler.from(new Handler()))
                .subscribe(shader -> debugGLSurfaceView.queueEvent(() -> renderer.updateVertexShader(shader)));

        fragSub = programBus
                .fragmentShaderBus()
                .debounce(500, TimeUnit.MILLISECONDS)
                .doOnNext(shader -> Timber.d("Fragment Shader code changed."))
                .observeOn(HandlerScheduler.from(new Handler()))
                .subscribe(shader -> debugGLSurfaceView.queueEvent(() -> renderer.updateFragmentShader(shader)));
    }

    private void unsubscribeProgramBus() {
        geoSub.unsubscribe();
        vertexSub.unsubscribe();
        fragSub.unsubscribe();
    }
}
