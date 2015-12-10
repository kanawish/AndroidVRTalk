package com.kanawish.gamepadmapper.service;

import android.accessibilityservice.AccessibilityService;
import android.content.Intent;
import android.util.Log;
import android.view.KeyEvent;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.functions.Action1;
import rx.subjects.BehaviorSubject;
import rx.subjects.Subject;

public class GamepadAccessibilityService extends AccessibilityService {
    private static final String TAG = GamepadAccessibilityService.class.getSimpleName();

    private final Subject<AccessibilityEvent,AccessibilityEvent> accEventBus;
    {
        // Behavior will provide latest value and subsequent changes to subscribers.
        BehaviorSubject<AccessibilityEvent> subject = BehaviorSubject.create();
        accEventBus = subject.toSerialized();
    }

    private final Subject<KeyEvent,KeyEvent> keyEventBus;
    {
        // Behavior will provide latest value and subsequent changes to subscribers.
        BehaviorSubject<KeyEvent> subject = BehaviorSubject.create();
        keyEventBus = subject.toSerialized();
    }

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        Log.d(TAG, "onServiceConnected() called");

        keyEventBus
            .doOnNext(keyEvent -> Log.d(TAG,String.format("Observed keyEvent %d",keyEvent.getKeyCode())))
            .filter(keyEvent -> keyEvent.getAction()==KeyEvent.ACTION_UP && keyEvent.getKeyCode()==61)
            .map(keyEvent -> performGlobalAction(GLOBAL_ACTION_RECENTS))
            .doOnNext(result -> Log.d(TAG,String.format("performGlobalAction(GLOBAL_ACTION_RECENTS) == %b",result)))
            .filter(result->result)
            .delay(500, TimeUnit.MILLISECONDS) // TODO: Could we delay on receiving something on different stream?
            .map(result->getRootInActiveWindow().getChild(0).getChild(0))
            .flatMap(node->{
                List<AccessibilityNodeInfo> children = new ArrayList<AccessibilityNodeInfo>();
                for(int i = 0 ; i < node.getChildCount(); i++) children.add(node.getChild(i));
                return Observable.from(children);
            })
            .doOnNext(node -> Log.d(TAG, String.format("description %s",node.getContentDescription())))
//            .map(node->
//            {
//                List<AccessibilityNodeInfo> children = new ArrayList<AccessibilityNodeInfo>();
//                for(int i = 0 ; i < node.getChildCount(); i++) children.add(node.getChild(i));
//
//                return node.getChild(0).performAction(AccessibilityNodeInfo.ACTION_CLICK);
//            })
            .doOnError(throwable -> Log.d(TAG, "Caught throwable, will retry()", throwable))
            .retry()
            .subscribe();
    }


    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
//        Log.d(TAG,String.format("Acc. event %d", event.getAction()));
        accEventBus.onNext(event);
    }

    @Override
    protected boolean onKeyEvent(KeyEvent event) {
        Log.d(TAG,String.format("onKeyEvent() %d", event.getKeyCode()));
        keyEventBus.onNext(event);

        return false;
    }

    @Override
    protected boolean onGesture(int gestureId) {
        Log.d(TAG,String.format("Gesture event %d", gestureId));
        return super.onGesture(gestureId);
    }

    @Override
    public void onInterrupt() {
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }
}
