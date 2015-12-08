package com.kanawish.gamepadmapper.service;

import android.accessibilityservice.AccessibilityService;
import android.content.Intent;
import android.util.Log;
import android.view.KeyEvent;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

public class GamepadAccessibilityService extends AccessibilityService {
    private static final String TAG = GamepadAccessibilityService.class.getSimpleName();
    private AccessibilityNodeInfo nodeInfo;

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();

    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        Log.d(TAG,String.format("Acc. event %s", event.describeContents()));
        nodeInfo = event.getSource();
    }

    @Override
    protected boolean onKeyEvent(KeyEvent event) {
        Log.d(TAG,String.format("Key event! %d", event.getKeyCode()));
        if(nodeInfo!=null) nodeInfo.performAction(AccessibilityNodeInfo.ACTION_CLICK);
        return true;

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
