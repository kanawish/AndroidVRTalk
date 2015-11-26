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

        public Obj() {
        }

        public Obj(float[] vertices, float[] normals, Instanced instanced) {
            this.v = vertices;
            this.n = normals;
            this.i = instanced;
        }
    }

    public static class Instanced {
        public int instancedCount;
        public float[] t; // translation vec3f
        public float[] r; // rotation vec3f
        public float[] s; // scale vec3f
        public float[] c; // color vec4f
        public float[] p; // params vec4f (These can be used for anything in the vector shader.)
        public int[] m; // mode, intended to toggle between shader modes.

        public Instanced() {
        }

        public Instanced(int instancedCount, float[][] params) {
            this.instancedCount = instancedCount;
            t = params[0];
            r = params[1];
            s = params[2];
            c = params[3];
            p = params[4];
        }
    }

    public List<Obj> objs;
}
