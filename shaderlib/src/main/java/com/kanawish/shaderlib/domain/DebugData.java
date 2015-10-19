package com.kanawish.shaderlib.domain;

/**
 * Created by kanawish on 15-04-27.
 */
public class DebugData {
    Type type = Type.RIGHT;
    String line1 = "";
    String line2 = "";
    String line3 = "";
    String line4 = "";
    boolean compileError = false;
    float fps = 0 ;

    public DebugData() {
    }

    public DebugData(Type type, String line1, String line2, String line3, String line4, boolean compileError, float fps) {
        this.type = type;
        this.line1 = line1;
        this.line2 = line2;
        this.line3 = line3;
        this.line4 = line4;
        this.compileError = compileError;
        this.fps = fps;
    }

    public boolean isCompileError() {
        return compileError;
    }

    public void setCompileError(boolean compileError) {
        this.compileError = compileError;
    }

    public float getFps() {
        return fps;
    }

    public void setFps(float fps) {
        this.fps = fps;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public String getLine1() {
        return line1;
    }

    public void setLine1(String line1) {
        this.line1 = line1;
    }

    public String getLine2() {
        return line2;
    }

    public void setLine2(String line2) {
        this.line2 = line2;
    }

    public String getLine3() {
        return line3;
    }

    public void setLine3(String line3) {
        this.line3 = line3;
    }

    public String getLine4() {
        return line4;
    }

    public void setLine4(String line4) {
        this.line4 = line4;
    }

    public enum Type {RIGHT, LEFT}
}
