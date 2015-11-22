package com.kanawish.androidvrtalk.domain;

import com.firebase.client.DataSnapshot;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.kanawish.shaderlib.domain.GeometryManager;
import com.kanawish.shaderlib.model.GeometryData;
import com.kanawish.shaderlib.utils.IOUtils;

import java.io.IOException;

import javax.inject.Inject;

import timber.log.Timber;


/**
 * Created by ecaron on 15-10-18.
 */
public class GeoScriptEventListener implements ValueEventListener {
    @Inject
    public GeoScriptEventListener() {
    }

    @Override
    public void onDataChange(DataSnapshot dataSnapshot) {

    }

    @Override
    public void onCancelled(FirebaseError firebaseError) {
        Timber.d(firebaseError.getMessage());
    }
}
