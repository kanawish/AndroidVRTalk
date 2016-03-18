package com.kanawish.shaderlib.model;

import com.kanawish.shaderlib.utils.SimpleGLUtils;

import java.nio.FloatBuffer;

/**
 * Built from a GeometryData object, all the buffers that need to be passed
 * to the OpenGl pipeline.
 *
 * Created by ecaron on 15-11-18.
 */
public class GeometryBufferData {

    private int vertexCount = 0 ;

    private FloatBuffer vertices;
    private FloatBuffer normals;

    private int instancedCount ;
    private FloatBuffer translations;
    private FloatBuffer rotations;
    private FloatBuffer scales;
    private FloatBuffer colors;
    private FloatBuffer parameters;

    public GeometryBufferData(GeometryData.Obj obj) {
        vertexCount = obj.v.length/3; // NOTE: Should always be == to n.length/3, but not enforcing for now.
        vertices = SimpleGLUtils.createFloatBuffer(obj.v);
        normals = SimpleGLUtils.createFloatBuffer(obj.n);

        if (obj.i != null) {
            instancedCount = obj.i.instancedCount;
            if (obj.i.t != null) translations = SimpleGLUtils.createFloatBuffer(obj.i.t);
            if (obj.i.r != null) rotations = SimpleGLUtils.createFloatBuffer(obj.i.r);
            if (obj.i.s != null) scales = SimpleGLUtils.createFloatBuffer(obj.i.s);
            if (obj.i.c != null) colors = SimpleGLUtils.createFloatBuffer(obj.i.c);
            if (obj.i.p != null) parameters = SimpleGLUtils.createFloatBuffer(obj.i.p);
        }
    }

    public int getVertexCount() {
        return vertexCount;
    }

    public FloatBuffer getVertices() {
        return vertices;
    }

    public FloatBuffer getNormals() {
        return normals;
    }

    public int getInstancedCount() {
        return instancedCount;
    }

    public FloatBuffer getTranslations() {
        return translations;
    }

    public FloatBuffer getRotations() {
        return rotations;
    }

    public FloatBuffer getScales() {
        return scales;
    }

    public FloatBuffer getColors() {
        return colors;
    }

    public FloatBuffer getParameters() {
        return parameters;
    }
}
