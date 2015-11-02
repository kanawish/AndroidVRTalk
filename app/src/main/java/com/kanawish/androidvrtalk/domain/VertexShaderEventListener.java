package com.kanawish.androidvrtalk.domain;

import com.firebase.client.DataSnapshot;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import javax.inject.Inject;

/**
 * Created by ecaron on 15-10-18.
 */
public class VertexShaderEventListener implements ValueEventListener {

    @Inject
    public VertexShaderEventListener() {
    }

    @Override
    public void onDataChange(DataSnapshot dataSnapshot) {

    }

    @Override
    public void onCancelled(FirebaseError firebaseError) {

    }
}
