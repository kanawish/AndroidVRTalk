package com.kanawish.shaderlib.model;

/**
 * Created by kanawish on 15-08-05.
 */ // TODO: Move all this to a "Manager"
public class ShaderData {
    String code;

    public ShaderData() {
    }

    public ShaderData(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}
