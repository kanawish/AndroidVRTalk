package com.kanawish.androidvrtalk.domain;

import android.app.Application;
import android.content.Context;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.kanawish.androidvrtalk.BuildConfig;
import com.kanawish.androidvrtalk.VrTalkApp;
import com.kanawish.shaderlib.domain.GeometryManager;
import com.kanawish.shaderlib.domain.PipelineProgramBus;
import com.kanawish.shaderlib.model.GeometryData;
import com.kanawish.shaderlib.model.ScriptData;
import com.kanawish.shaderlib.model.ShaderData;
import com.kanawish.shaderlib.utils.IOUtils;

import java.io.IOException;

import javax.inject.Inject;
import javax.inject.Singleton;

import rx.Observable;
import rx.subjects.BehaviorSubject;
import rx.subjects.Subject;
import timber.log.Timber;

/**
 * Created by ecaron on 15-10-27.
 *
 * This will publish geoScript & shader events.
 */
@Singleton
public class FirebaseManager {

    public static final String GEO_SCRIPT = "geo_script";
    public static final String VERTEX_SHADER = "vertex_shader";
    public static final String FRAGMENT_SHADER = "fragment_shader";

    private final Firebase firebaseRef;

    private final ValueEventListener geoListener;
    private final ValueEventListener vertexListener;
    private final ValueEventListener fragmentListener;

    @Inject
    public FirebaseManager(Application app, PipelineProgramBus bus) {
        Firebase.setAndroidContext(app);

        firebaseRef = new Firebase(BuildConfig.FIREBASE_HOST);

        geoListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                final ScriptData value = dataSnapshot.getValue(ScriptData.class);
                if( value == null ) return ;
                bus.publishGeoScript(value.getCode());
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
                Timber.e(firebaseError.toException(), firebaseError.toString());
            }
        };
        firebaseRef.child(GEO_SCRIPT).addValueEventListener(geoListener);

        vertexListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                final ShaderData value = dataSnapshot.getValue(ShaderData.class);
                if( value == null ) return ;
                bus.publishVertexShader(value.getCode());
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
                Timber.e(firebaseError.toException(), firebaseError.toString());
            }
        };
        firebaseRef.child(VERTEX_SHADER).addValueEventListener(vertexListener);

        fragmentListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                final ShaderData value = dataSnapshot.getValue(ShaderData.class);
                if( value == null ) return ;
                bus.publishFragmentShader(value.getCode());
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
                Timber.e(firebaseError.toException(), firebaseError.toString());
            }
        };
        firebaseRef.child(FRAGMENT_SHADER).addValueEventListener(fragmentListener);
    }

    /**
     * For outside users, we simply expose the firebase for now.
     * @return firebaseRef
     */
    public Firebase getFirebaseRef() {
        return firebaseRef;
    }

    /**
     * What we need usually would give up 'onDestroy'.
     *
     * My understanding right now since this Manager is effectively an Application-level
     * singleton, we should not need to call this directly.
     *
     * Including destroy for the case where we might want to make this an Activity-level singleton.
     *
     */
    public void destroy() {
        firebaseRef.getRoot().child("fragment_shader").removeEventListener(fragmentListener);
        firebaseRef.getRoot().child("vertex_shader").removeEventListener(vertexListener);
        firebaseRef.getRoot().child("geoData").removeEventListener(geoListener);
    }

}
