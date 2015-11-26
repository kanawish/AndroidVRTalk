package com.kanawish.androidvrtalk.ui;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.vrtoolkit.cardboard.CardboardActivity;
import com.google.vrtoolkit.cardboard.CardboardView;
import com.kanawish.androidvrtalk.R;
import com.kanawish.androidvrtalk.domain.GeoScriptEventListener;
import com.kanawish.androidvrtalk.domain.ScriptManager;
import com.kanawish.androidvrtalk.domain.VertexShaderEventListener;
import com.kanawish.androidvrtalk.injection.Injector;
import com.kanawish.common.InputData;
import com.kanawish.common.InputDataBuilder;
import com.kanawish.shaderlib.domain.CameraManager;
import com.kanawish.shaderlib.domain.DebugData;
import com.kanawish.shaderlib.domain.GeometryManager;
import com.kanawish.shaderlib.domain.PipelineProgramBus;
import com.kanawish.shaderlib.gl.LiveStereoRenderer;
import com.kanawish.shaderlib.utils.IOUtils;


import java.io.IOException;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import dagger.ObjectGraph;
import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.android.schedulers.HandlerScheduler;
import rx.functions.Action1;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;
import timber.log.Timber;


/*

NOTE: The following diagrams can be viewed in Android Studio using the PlantUML plugin.

VANILLA OPENGL CLASS DIAGRAM

@startuml
View <|-- GLSurfaceView
GLSurfaceView *-right- GLSurfaceView.Renderer
hide View members
hide GLSurfaceView members
hide GLSurfaceView.Renderer attributes

class View

class GLSurfaceView {
 - GLSurfaceView.Renderer renderer
 + setRenderer()
}

class GLSurfaceView.Renderer {
    +onSurfaceCreated()
    +onSurfaceChanged()
    +onDrawFrame()
}
@enduml


VANILLA THREADING / RENDER LOOP

@startuml
    control UI_Thread
    control GL_Render_Thread

    GL_Render_Thread -> Renderer: draw( )
    activate Renderer
    UI_Thread --> GL_Render_Thread: queueEvent(Runnable)
    GL_Render_Thread <-- Renderer:
    deactivate Renderer

    GL_Render_Thread <-- GL_Render_Thread: runQueued()

GL_Render_Thread -> Renderer: draw( )
activate Renderer
GL_Render_Thread <-- Renderer:
deactivate Renderer

GL_Render_Thread -> Renderer: draw( )
activate Renderer
GL_Render_Thread <-- Renderer:
deactivate Renderer

 ...etc......
 @enduml


CARDBOARD CLASS DIAGRAM

@startuml
hide attributes
hide CardboardView methods
hide GLSurfaceView methods

class CardboardActivity {
    +void onCardboardTrigger ()
}
class GLSurfaceView
class CardboardView
class CardboardView.StereoRenderer {
    +onRendererShutdown()
    +onSurfaceChanged(width, height)
    +onSurfaceCreated(config)
    +onNewFrame(headTransform)
    +onDrawEye(eye)
    +onFinishFrame(viewport)
}

GLSurfaceView <|---CardboardView
CardboardActivity *-- CardboardView
CardboardView*--CardboardView.StereoRenderer

@enduml

 */
public class VrTalkActivity extends CardboardActivity {

    private GestureDetector.OnDoubleTapListener doubleTapListener = new GestureDetector.OnDoubleTapListener() {
        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            InputData data = new InputDataBuilder().setType(InputData.SINGLE_TAP_CONFIRMED).setEvent1(e).createInputData();

            return true;
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            // ?
            return true;
        }

        @Override
        public boolean onDoubleTapEvent(MotionEvent e) {
            InputData data = new InputDataBuilder().setType(InputData.DOUBLE_TAP).setEvent1(e).createInputData();
            return true;
        }
    };

    private GestureDetector.OnGestureListener gestureListener = new GestureDetector.OnGestureListener() {
        // Added attribute to avoid new CameraManager.CameraData() perf overhead.
        CameraManager.CameraData cd = new CameraManager.CameraData();

        @Override
        public boolean onDown(MotionEvent e) {
            InputData data = new InputDataBuilder().setType(InputData.DOWN).setEvent1(e).createInputData();

            return true;
        }

        @Override
        public void onShowPress(MotionEvent e) {
        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            InputData data = new InputDataBuilder().setType(InputData.SINGLE_TAP_UP).setEvent1(e).createInputData();

            return true;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            InputData data = new InputDataBuilder()
                    .setType(InputData.SCROLL)
                    .setEvent1(e1)
                    .setEvent2(e2)
                    .setParamX(distanceX)
                    .setParamY(distanceY)
                    .createInputData();

            Timber.d("onScroll - %1.2f,%1.2f", distanceX,distanceY);
            cd.setCameraRotation(new float[] {distanceX,distanceY,0});
            cd.setCameraTranslation(new float[] {0,0,0});

            cameraManager.applyDelta(cd);
            return true;
        }

        @Override
        public void onLongPress(MotionEvent e) {
            InputData data = new InputDataBuilder().setType(InputData.LONG_PRESS).setEvent1(e).createInputData();
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            InputData data = new InputDataBuilder()
                    .setType(InputData.FLING)
                    .setEvent1(e1)
                    .setEvent2(e2)
                    .setParamX(velocityX)
                    .setParamY(velocityY)
                    .createInputData();

            return true;
        }
    };

    @Bind(R.id.lline1)
    TextView lline1;
    @Bind(R.id.lline2)
    TextView lline2;
    @Bind(R.id.lline3)
    TextView lline3;
    @Bind(R.id.lline4)
    TextView lline4;
    @Bind(R.id.debugTextLeft)
    LinearLayout debugTextLeft;

    @Bind(R.id.rline1)
    TextView rline1;
    @Bind(R.id.rline2)
    TextView rline2;
    @Bind(R.id.rline3)
    TextView rline3;
    @Bind(R.id.rline4)
    TextView rline4;
    @Bind(R.id.debugTextRight)
    LinearLayout debugTextRight;

    @Bind(R.id.fpsTextView)
    TextView fpsTextView;

    @Bind(R.id.ui_layout)
    RelativeLayout uiLayout;

    @Bind(R.id.cardboard_view)
    CardboardView cardboardView;

    private VertexShaderEventListener vertexShaderEventListener;
    private VertexShaderEventListener fragmentShaderEventListener;
    private GeoScriptEventListener geoEventListener;


    private ObjectGraph activityGraph;

    @Inject ScriptManager scriptManager;

    // The camera manager will be used to help us move the viewpoint in our scene, etc.
    @Inject CameraManager cameraManager;
    @Inject PipelineProgramBus programBus;
    @Inject GeometryManager geometryManager;

    // TODO: Inject
    LiveStereoRenderer renderer;

    private String geoWrapper;

    // Subscriptions to the code updaters
    private Subscription geoSub;
    private Subscription vertexSub;
    private Subscription fragSub;


    // UI / CONTROLS
    private GestureDetector gestureDetector;

    @Override
    public Object getSystemService(@NonNull String name) {
        if (Injector.matchesService(name)) {
            return activityGraph;
        }
        return super.getSystemService(name);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ObjectGraph appGraph = Injector.obtain(getApplication());
        appGraph.inject(this);
        // Not really used yet, but added this to demonstrate best practice.
        activityGraph = appGraph.plus(new ActivityModule(this));

        setContentView(R.layout.vr_talk_ui);
        ButterKnife.bind(this);

        // Configure the cardboardView. There's a lot of available options, here's two.
        cardboardView.setVRModeEnabled(false);
        cardboardView.setDistortionCorrectionEnabled(false); // FIXME: Currently broken

        // Create the renderer that does the actual drawing.
        renderer = new LiveStereoRenderer(this,cameraManager);
        // Assign the Renderer to the View...
        cardboardView.setRenderer(renderer);
        // Then assign the view to this CardboardActivity
        setCardboardView(cardboardView);

        // This will subscribe to a head tracking info stream, we overlay it to help debug scenes.
        subscribeToDebugPublisher(renderer.getDebugOutputPublishSubject());

        // Load geoWrapper from local storage.
        try {
            geoWrapper = IOUtils.loadStringFromAsset(this, "js/wrapper.js");
        } catch (IOException e) {
            Timber.e(e, "Failed to load 'js/wrapper.js'");
            throw new RuntimeException("Critical failure, app is missing 'wrapper.js' asset.");
        }


        // TODO: An explanatory overlay would be nice, like in some examples.

        // Setup touch input
        gestureDetector = new GestureDetector(VrTalkActivity.this, gestureListener);
        gestureDetector.setOnDoubleTapListener(doubleTapListener);

        cardboardView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return gestureDetector.onTouchEvent(event);
            }
        });

    }

    @Override
    public void onStart() {
        super.onStart();

    }

    @Override
    protected void onResume() {
        super.onResume();

        subscribeProgramBus();

        renderer.play();
    }

    public void subscribeProgramBus() {
        geoSub = programBus
                .geoScriptBus()
                .debounce(500, TimeUnit.MILLISECONDS)
                .map(script -> String.format(geoWrapper, script)) // TODO: Came from original rhino setup, might be removeable
                .observeOn(Schedulers.computation())
//                .map(script -> GeometryManager.rhinoGeometryData(script))
//                .map(script -> GeometryManager.duktapeGeometryData(script))
                .map(geometryManager::webviewGeometryData)
                .doOnError(throwable -> Timber.e(throwable, "GeometryScript failed to execute."))
                .retryWhen(e -> e.flatMap( i -> Observable.timer(5000, TimeUnit.MILLISECONDS)))
                .subscribe(data -> renderer.updateGeometryData(data));

        vertexSub = programBus
                .vertexShaderBus()
                .debounce(500, TimeUnit.MILLISECONDS)
                .doOnNext(shader -> Timber.d("Vector Shader code changed."))
                .observeOn(HandlerScheduler.from(new Handler()))
                .subscribe(shader -> cardboardView.queueEvent(() -> renderer.updateVertexShader(shader)));

        fragSub = programBus
                .fragmentShaderBus()
                .debounce(500, TimeUnit.MILLISECONDS)
                .doOnNext(shader -> Timber.d("Fragment Shader code changed."))
                .observeOn(HandlerScheduler.from(new Handler()))
                .subscribe(shader -> cardboardView.queueEvent(() -> renderer.updateFragmentShader(shader)));
    }


    @Override
    protected void onPause() {
        super.onPause();
        renderer.pause();

        // TODO: Double check, was there a good reason I'd put this in 'onStop'? Seems like an error.
        geoSub.unsubscribe();
        vertexSub.unsubscribe();
        fragSub.unsubscribe();
    }

    public void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        activityGraph = null;
        super.onDestroy();
    }

    // Moved this out of the onCreate() method to avoid confusing people new to Rx.
    private void subscribeToDebugPublisher(PublishSubject<DebugData> publisher) {
        publisher
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(new Action1<DebugData>() {
                int redColor = getResources().getColor(R.color.red);
                int greenColor = getResources().getColor(R.color.green);

                @Override
                public void call(DebugData debugData) {
                    if (debugData.getType() == DebugData.Type.RIGHT) {
                        rline1.setText(debugData.getLine1());
                        rline2.setText(debugData.getLine2());
                        rline3.setText(debugData.getLine3());
                        rline4.setText(debugData.getLine4());
                    } else {
                        lline1.setText(debugData.getLine1());
                        lline2.setText(debugData.getLine2());
                        lline3.setText(debugData.getLine3());
                        lline4.setText(debugData.getLine4());
                        fpsTextView.setText(String.format("%.0fms / %.0ffps", debugData.getFps(), 1000.0 / debugData.getFps()));
                        fpsTextView.setTextColor(debugData.isCompileError() ? redColor : greenColor);
                    }
                }
            });
    }


    static final float D = 0.1f;

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        boolean handled = true ;
        switch(keyCode) {
            case KeyEvent.KEYCODE_A:
                cameraManager.applyTranslationDelta(D, 0f, 0f);
                break;
            case KeyEvent.KEYCODE_S:
                cameraManager.applyTranslationDelta(0f, 0f, -D); // -z is forward
                break;
            case KeyEvent.KEYCODE_D:
                cameraManager.applyTranslationDelta(-D, 0f, 0f);
                break;
            case KeyEvent.KEYCODE_W:
                cameraManager.applyTranslationDelta(0f, 0f, D);
                break;
            case KeyEvent.KEYCODE_Q:
                cameraManager.applyTranslationDelta(0f, -D, 0f);
                break;
            case KeyEvent.KEYCODE_E:
                cameraManager.applyTranslationDelta(0f, D, 0f);
                break;
            default:
                handled = false;
                break;
        }

        return handled;
    }

}
