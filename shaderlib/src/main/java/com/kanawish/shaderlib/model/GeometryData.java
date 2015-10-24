package com.kanawish.shaderlib.model;

import java.util.List;

/**
 * Data we'll get from remote sources to feed our geometry.
 *
 * Required: at least 1 obj with v and n data.
 * Optional: any of the per-instance items (t,r,s or p).
 */
public class GeometryData {

    public static class Obj {
        public float[] v;   // vertices
        public float[] n;   // normals
        public Instanced i; // instance data
    }

    public static class Instanced {
        public int instancedCount;
        public float[] t; // translation vec3f
        public float[] r; // rotation vec3f
        public float[] s; // scale vec3f
        public float[] c; // color vec4f
        public float[] p; // params vec4f (These can be used for anything in the vector shader.)
    }

    public List<Obj> objs;
}
