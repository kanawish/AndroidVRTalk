package com.kanawish.androidvrtalk;

import android.os.Bundle;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.vrtoolkit.cardboard.CardboardActivity;
import com.google.vrtoolkit.cardboard.CardboardView;
import com.kanawish.androidvrtalk.domain.GeoEventListener;
import com.kanawish.androidvrtalk.domain.ShaderEventListener;
import com.kanawish.common.InputData;
import com.kanawish.common.InputDataBuilder;
import com.kanawish.shaderlib.domain.CameraManager;
import com.kanawish.shaderlib.domain.DebugData;
import com.kanawish.shaderlib.gl.LiveStereoRenderer;
import com.kanawish.shaderlib.model.GeometryData;
import com.kanawish.shaderlib.model.ShaderData;


import butterknife.Bind;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
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

    private ShaderEventListener vertexShaderEventListener;
    private ShaderEventListener fragmentShaderEventListener;
    private GeoEventListener geoEventListener;

    LiveStereoRenderer renderer;


    // TODO: Add dagger 1 injection support. @Inject
    CameraManager cameraManager;


    // UI / CONTROLS

    private GestureDetector gestureDetector;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // todo: butterknife bind this?
        setContentView(R.layout.vr_talk_ui);
        butterknife.ButterKnife.bind(this);

        // The camera manager will be used to help us move the viewpoint in our scene, etc.

        cameraManager = new CameraManager();

        // Configure the cardboardView. There's a lot of available options here...
        // TODO: Try to demo and explain some more of these in the talk.
        cardboardView.setVRModeEnabled(true);
        cardboardView.setDistortionCorrectionEnabled(false);

        // Create the renderer that does the actual drawing.
        renderer = new LiveStereoRenderer(this,cameraManager);
        // Assign the Renderer to the View...
        cardboardView.setRenderer(renderer);
        // Then assign the view to this CardboardActivity
        setCardboardView(cardboardView);

        // This will subscribe to a head tracking info stream, we overlay it to help debug scenes.
        subscribeToDebugPublisher(renderer.getDebugOutputPublishSubject());

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

        // TODO: We used to use Firebase, abstract it for talk, or simply implement credential config cleanly.
        // Geometry Data
        geoEventListener = new GeoEventListener() {
            public void onDataChange(GeometryData value) {
                Timber.d("GeoData received.");
                // NOTE: Between shaders and geo, I have 2 queueing mechanisms here, we'll want only the 1...
                // I believe the one I use with fragments could cause race conditions (not sure), so I should try to
                // come up with a hybrid between geo and shader queuing? Or just validate if there's a problem or not.
                final boolean succeeded = renderer.updateGeometryData(value);
                if( !succeeded ) Timber.w("Failed to queue updated GeometryData.");
            }

            public void onCancelled(String errorMsg) {
                Timber.d(errorMsg);
            }
        };
//        mFirebaseRef.getRoot().child("geoData").addValueEventListener(geoEventListener);

        // Vertex Shader Data
        vertexShaderEventListener = new ShaderEventListener() {
            public void onDataChange(final ShaderData model) {
                Timber.d("Vector Shader code changed.");

                // TODO: Debounce.
                getCardboardView().queueEvent(new Runnable() {
                    @Override
                    public void run() {
                        renderer.updateVertexShader(model.getCode());
                    }
                });
            }

            public void onCancelled(String errorMsg) {
                Timber.d(errorMsg);
            }
        };
//        mFirebaseRef.getRoot().child("vertex_shader").addValueEventListener(vertexShaderEventListener);

        // Fragment Shader Data
        fragmentShaderEventListener = new ShaderEventListener() {
            public void onDataChange(final ShaderData model) {
                Timber.d("Fragment Shader code changed.");

                // TODO: Debounce.
                getCardboardView().queueEvent(new Runnable() {
                    @Override
                    public void run() {
                        renderer.updateFragmentShader(model.getCode());
                    }
                });
            }

            public void onCancelled(String errorMsg) {
                Timber.d(errorMsg);
            }
        };
//        mFirebaseRef.getRoot().child("fragment_shader").addValueEventListener(fragmentShaderEventListener);

    }

    @Override
    protected void onResume() {
        super.onResume();
        renderer.play();
    }

    @Override
    protected void onPause() {
        super.onPause();
        renderer.pause();
    }

    public void onStop() {
        super.onStop();
//        mFirebaseRef.getRoot().child("fragment_shader").removeEventListener(fragmentShaderEventListener);
//        mFirebaseRef.getRoot().child("vertex_shader").removeEventListener(vertexShaderEventListener);
//        mFirebaseRef.getRoot().child("geoData").removeEventListener(geoEventListener);

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
