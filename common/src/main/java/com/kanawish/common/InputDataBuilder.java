package com.kanawish.common;

import android.view.MotionEvent;

public class InputDataBuilder {
    private byte type;
    private MotionEvent event1;
    private MotionEvent event2;
    private float paramX = -1;
    private float paramY = -1;

    public InputDataBuilder setType(byte type) {
        this.type = type;
        return this;
    }

    public InputDataBuilder setEvent1(MotionEvent event1) {
        this.event1 = event1;
        return this;
    }

    public InputDataBuilder setEvent2(MotionEvent event2) {
        this.event2 = event2;
        return this;
    }

    public InputDataBuilder setParamX(float paramX) {
        this.paramX = paramX;
        return this;
    }

    public InputDataBuilder setParamY(float paramY) {
        this.paramY = paramY;
        return this;
    }

    public InputData createInputData() {
        return new InputData(type, event1, event2, paramX, paramY);
    }
}