package com.kanawish.common;

import android.os.Parcel;
import android.os.Parcelable;
import android.view.MotionEvent;

/**
 * Created by kanawish on 15-06-25.
 */
public class InputData implements Parcelable {

    public static final byte DOWN = 1 ;
    public static final byte SINGLE_TAP_UP = 2 ;
    public static final byte SCROLL = 3 ;
    public static final byte LONG_PRESS = 4 ;
    public static final byte FLING = 5 ;

    public static final byte SINGLE_TAP_CONFIRMED = 6 ;
    public static final byte DOUBLE_TAP = 7 ;

    // TODO: Check if packing booleans in a byte would improve anything.
    private byte type ;

    private MotionEvent event1 ;
    private MotionEvent event2 ;

    private float paramX ;
    private float paramY ;

    InputData(byte type, MotionEvent event1, MotionEvent event2, float paramX, float paramY) {
        this.setType(type);
        this.setEvent1(event1);
        this.setEvent2(event2);
        this.setParamX(paramX);
        this.setParamY(paramY);
    }

    protected InputData(Parcel in) {
        setType(in.readByte());
        setEvent1(in.<MotionEvent>readParcelable(MotionEvent.class.getClassLoader()));
        setEvent2(in.<MotionEvent>readParcelable(MotionEvent.class.getClassLoader()));
        setParamX(in.readFloat());
        setParamY(in.readFloat());
    }

    public static final Creator<InputData> CREATOR = new Creator<InputData>() {
        @Override
        public InputData createFromParcel(Parcel in) {
            return new InputData(in);
        }

        @Override
        public InputData[] newArray(int size) {
            return new InputData[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeByte(getType());
        dest.writeParcelable(getEvent1(), flags);
        dest.writeParcelable(getEvent2(), flags);
        dest.writeFloat(getParamX());
        dest.writeFloat(getParamY());
    }

    public byte getType() {
        return type;
    }

    public void setType(byte type) {
        this.type = type;
    }

    public MotionEvent getEvent1() {
        return event1;
    }

    public void setEvent1(MotionEvent event1) {
        this.event1 = event1;
    }

    public MotionEvent getEvent2() {
        return event2;
    }

    public void setEvent2(MotionEvent event2) {
        this.event2 = event2;
    }

    public float getParamX() {
        return paramX;
    }

    public void setParamX(float paramX) {
        this.paramX = paramX;
    }

    public float getParamY() {
        return paramY;
    }

    public void setParamY(float paramY) {
        this.paramY = paramY;
    }
}
