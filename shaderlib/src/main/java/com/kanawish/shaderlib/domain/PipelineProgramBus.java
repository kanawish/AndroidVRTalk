package com.kanawish.shaderlib.domain;

import javax.inject.Inject;
import javax.inject.Singleton;

import rx.Observable;
import rx.subjects.BehaviorSubject;
import rx.subjects.Subject;

/**
 * Created by ecaron on 15-10-28.
 */
@Singleton
public class PipelineProgramBus {

    // Geometry ScriptData bus.
    private final Subject<String, String> geoScriptBus;
    {
        // Behavior will provide latest value and subsequent changes to subscribers.
        BehaviorSubject<String> subject = BehaviorSubject.create();
        geoScriptBus = subject.toSerialized();
    }

    private final Subject<String,String> vertexShaderBus;
    {
        // Behavior will provide latest value and subsequent changes to subscribers.
        BehaviorSubject<String> subject = BehaviorSubject.create();
        vertexShaderBus = subject.toSerialized();
    }

    private final Subject<String,String> fragmentShaderBus;
    {
        // Behavior will provide latest value and subsequent changes to subscribers.
        BehaviorSubject<String> subject = BehaviorSubject.create();
        fragmentShaderBus = subject.toSerialized();
    }

    @Inject
    public PipelineProgramBus() {
    }

    public Observable<String> geoScriptBus() {
        return geoScriptBus;
    }

    public void publishGeoScript(String script) {
        geoScriptBus.onNext(script);
    }

    public Observable<String> vertexShaderBus() {
        return vertexShaderBus;
    }

    public void publishVertexShader(String shader) {
        vertexShaderBus.onNext(shader);
    }

    public Observable<String> fragmentShaderBus() {
        return fragmentShaderBus;
    }

    public void publishFragmentShader(String shader) {
        fragmentShaderBus.onNext(shader);
    }

}
