package com.kanawish.shaderlib.generation;

import com.kanawish.shaderlib.model.GeometryData;

import java.util.ArrayList;

import timber.log.Timber;

/**
 * Contains vertex, normal and color data.
 */
public final class DefaultModels {

    public static final float[] TRIANGLE_COORDS = new float[]{
        -1.0f, 1.0f, 1.0f,
        -1.0f, -1.0f, 1.0f,
        1.0f, 1.0f, 1.0f,
    };
    public static final float[] TRIANGLE_COLORS = new float[]{
        // front, green
        0f, 0.5273f, 0.2656f, 1.0f,
        0f, 0.5273f, 0.2656f, 1.0f,
        0f, 0.5273f, 0.2656f, 1.0f,
    };
    public static final float[] TRIANGLE_NORMALS = new float[]{
        // Front face
        0.0f, 0.0f, 1.0f,
        0.0f, 0.0f, 1.0f,
        0.0f, 0.0f, 1.0f,
    };

    public static final float[] CUBE_COORDS = new float[]{
        // Front face
        -1.0f, 1.0f, 1.0f,
        -1.0f, -1.0f, 1.0f,
        1.0f, 1.0f, 1.0f,
        -1.0f, -1.0f, 1.0f,
        1.0f, -1.0f, 1.0f,
        1.0f, 1.0f, 1.0f,

        // Right face
        1.0f, 1.0f, 1.0f,
        1.0f, -1.0f, 1.0f,
        1.0f, 1.0f, -1.0f,
        1.0f, -1.0f, 1.0f,
        1.0f, -1.0f, -1.0f,
        1.0f, 1.0f, -1.0f,

        // Back face
        1.0f, 1.0f, -1.0f,
        1.0f, -1.0f, -1.0f,
        -1.0f, 1.0f, -1.0f,
        1.0f, -1.0f, -1.0f,
        -1.0f, -1.0f, -1.0f,
        -1.0f, 1.0f, -1.0f,

        // Left face
        -1.0f, 1.0f, -1.0f,
        -1.0f, -1.0f, -1.0f,
        -1.0f, 1.0f, 1.0f,
        -1.0f, -1.0f, -1.0f,
        -1.0f, -1.0f, 1.0f,
        -1.0f, 1.0f, 1.0f,

        // Top face
        -1.0f, 1.0f, -1.0f,
        -1.0f, 1.0f, 1.0f,
        1.0f, 1.0f, -1.0f,
        -1.0f, 1.0f, 1.0f,
        1.0f, 1.0f, 1.0f,
        1.0f, 1.0f, -1.0f,

        // Bottom face
        1.0f, -1.0f, -1.0f,
        1.0f, -1.0f, 1.0f,
        -1.0f, -1.0f, -1.0f,
        1.0f, -1.0f, 1.0f,
        -1.0f, -1.0f, 1.0f,
        -1.0f, -1.0f, -1.0f,
    };

    public static final float[] CUBE_COLORS = new float[]{
        // front, green
        0f, 0.5273f, 0.2656f, 1.0f,
        0f, 0.5273f, 0.2656f, 1.0f,
        0f, 0.5273f, 0.2656f, 1.0f,
        0f, 0.5273f, 0.2656f, 1.0f,
        0f, 0.5273f, 0.2656f, 1.0f,
        0f, 0.5273f, 0.2656f, 1.0f,

        // right, blue
        0.0f, 0.3398f, 0.9023f, 1.0f,
        0.0f, 0.3398f, 0.9023f, 1.0f,
        0.0f, 0.3398f, 0.9023f, 1.0f,
        0.0f, 0.3398f, 0.9023f, 1.0f,
        0.0f, 0.3398f, 0.9023f, 1.0f,
        0.0f, 0.3398f, 0.9023f, 1.0f,

        // back, also green
        0f, 0.5273f, 0.2656f, 1.0f,
        0f, 0.5273f, 0.2656f, 1.0f,
        0f, 0.5273f, 0.2656f, 1.0f,
        0f, 0.5273f, 0.2656f, 1.0f,
        0f, 0.5273f, 0.2656f, 1.0f,
        0f, 0.5273f, 0.2656f, 1.0f,

        // left, also blue
        0.0f, 0.3398f, 0.9023f, 1.0f,
        0.0f, 0.3398f, 0.9023f, 1.0f,
        0.0f, 0.3398f, 0.9023f, 1.0f,
        0.0f, 0.3398f, 0.9023f, 1.0f,
        0.0f, 0.3398f, 0.9023f, 1.0f,
        0.0f, 0.3398f, 0.9023f, 1.0f,

        // top, red
        0.8359375f, 0.17578125f, 0.125f, 1.0f,
        0.8359375f, 0.17578125f, 0.125f, 1.0f,
        0.8359375f, 0.17578125f, 0.125f, 1.0f,
        0.8359375f, 0.17578125f, 0.125f, 1.0f,
        0.8359375f, 0.17578125f, 0.125f, 1.0f,
        0.8359375f, 0.17578125f, 0.125f, 1.0f,

        // bottom, also red
        0.8359375f, 0.17578125f, 0.125f, 1.0f,
        0.8359375f, 0.17578125f, 0.125f, 1.0f,
        0.8359375f, 0.17578125f, 0.125f, 1.0f,
        0.8359375f, 0.17578125f, 0.125f, 1.0f,
        0.8359375f, 0.17578125f, 0.125f, 1.0f,
        0.8359375f, 0.17578125f, 0.125f, 1.0f,
    };

    public static final float[] CUBE_FOUND_COLORS = new float[]{
        // front, yellow
        1.0f, 0.6523f, 0.0f, 1.0f,
        1.0f, 0.6523f, 0.0f, 1.0f,
        1.0f, 0.6523f, 0.0f, 1.0f,
        1.0f, 0.6523f, 0.0f, 1.0f,
        1.0f, 0.6523f, 0.0f, 1.0f,
        1.0f, 0.6523f, 0.0f, 1.0f,

        // right, yellow
        1.0f, 0.6523f, 0.0f, 1.0f,
        1.0f, 0.6523f, 0.0f, 1.0f,
        1.0f, 0.6523f, 0.0f, 1.0f,
        1.0f, 0.6523f, 0.0f, 1.0f,
        1.0f, 0.6523f, 0.0f, 1.0f,
        1.0f, 0.6523f, 0.0f, 1.0f,

        // back, yellow
        1.0f, 0.6523f, 0.0f, 1.0f,
        1.0f, 0.6523f, 0.0f, 1.0f,
        1.0f, 0.6523f, 0.0f, 1.0f,
        1.0f, 0.6523f, 0.0f, 1.0f,
        1.0f, 0.6523f, 0.0f, 1.0f,
        1.0f, 0.6523f, 0.0f, 1.0f,

        // left, yellow
        1.0f, 0.6523f, 0.0f, 1.0f,
        1.0f, 0.6523f, 0.0f, 1.0f,
        1.0f, 0.6523f, 0.0f, 1.0f,
        1.0f, 0.6523f, 0.0f, 1.0f,
        1.0f, 0.6523f, 0.0f, 1.0f,
        1.0f, 0.6523f, 0.0f, 1.0f,

        // top, yellow
        1.0f, 0.6523f, 0.0f, 1.0f,
        1.0f, 0.6523f, 0.0f, 1.0f,
        1.0f, 0.6523f, 0.0f, 1.0f,
        1.0f, 0.6523f, 0.0f, 1.0f,
        1.0f, 0.6523f, 0.0f, 1.0f,
        1.0f, 0.6523f, 0.0f, 1.0f,

        // bottom, yellow
        1.0f, 0.6523f, 0.0f, 1.0f,
        1.0f, 0.6523f, 0.0f, 1.0f,
        1.0f, 0.6523f, 0.0f, 1.0f,
        1.0f, 0.6523f, 0.0f, 1.0f,
        1.0f, 0.6523f, 0.0f, 1.0f,
        1.0f, 0.6523f, 0.0f, 1.0f,
    };

    public static final float[] CUBE_NORMALS = new float[]{
        // Front face
        0.0f, 0.0f, 1.0f,
        0.0f, 0.0f, 1.0f,
        0.0f, 0.0f, 1.0f,
        0.0f, 0.0f, 1.0f,
        0.0f, 0.0f, 1.0f,
        0.0f, 0.0f, 1.0f,

        // Right face
        1.0f, 0.0f, 0.0f,
        1.0f, 0.0f, 0.0f,
        1.0f, 0.0f, 0.0f,
        1.0f, 0.0f, 0.0f,
        1.0f, 0.0f, 0.0f,
        1.0f, 0.0f, 0.0f,

        // Back face
        0.0f, 0.0f, -1.0f,
        0.0f, 0.0f, -1.0f,
        0.0f, 0.0f, -1.0f,
        0.0f, 0.0f, -1.0f,
        0.0f, 0.0f, -1.0f,
        0.0f, 0.0f, -1.0f,

        // Left face
        -1.0f, 0.0f, 0.0f,
        -1.0f, 0.0f, 0.0f,
        -1.0f, 0.0f, 0.0f,
        -1.0f, 0.0f, 0.0f,
        -1.0f, 0.0f, 0.0f,
        -1.0f, 0.0f, 0.0f,

        // Top face
        0.0f, 1.0f, 0.0f,
        0.0f, 1.0f, 0.0f,
        0.0f, 1.0f, 0.0f,
        0.0f, 1.0f, 0.0f,
        0.0f, 1.0f, 0.0f,
        0.0f, 1.0f, 0.0f,

        // Bottom face
        0.0f, -1.0f, 0.0f,
        0.0f, -1.0f, 0.0f,
        0.0f, -1.0f, 0.0f,
        0.0f, -1.0f, 0.0f,
        0.0f, -1.0f, 0.0f,
        0.0f, -1.0f, 0.0f
    };

    public static final float[] FLOOR_COORDS = new float[]{
        200f, 0, -200f,
        -200f, 0, -200f,
        -200f, 0, 200f,
        200f, 0, -200f,
        -200f, 0, 200f,
        200f, 0, 200f,
    };

    public static final float[] FLOOR_NORMALS = new float[]{
        0.0f, 1.0f, 0.0f,
        0.0f, 1.0f, 0.0f,
        0.0f, 1.0f, 0.0f,
        0.0f, 1.0f, 0.0f,
        0.0f, 1.0f, 0.0f,
        0.0f, 1.0f, 0.0f,
    };

    public static final float[] FLOOR_COLORS = new float[]{
        0.0f, 0.3398f, 0.9023f, 1.0f,
        0.0f, 0.3398f, 0.9023f, 1.0f,
        0.0f, 0.3398f, 0.9023f, 1.0f,
        0.0f, 0.3398f, 0.9023f, 1.0f,
        0.0f, 0.3398f, 0.9023f, 1.0f,
        0.0f, 0.3398f, 0.9023f, 1.0f,
    };

    public static final GeometryData buildCube() {
        Timber.d("buildCube()");
        GeometryData data = new GeometryData();
        data.objs = new ArrayList<>();
        float deg = (float) (Math.PI / 2f);
        GeometryData.Instanced i =
            new GeometryData.Instanced(1, new float[][]{{-4.0f, 0.0f, -15f}, {deg/2, deg/2, 0}, {1, 1, 1}, {1.0f, 0.8f, 0.0f, 1}, {1,0,0,0}});
        GeometryData.Obj obj = new GeometryData.Obj(DefaultModels.CUBE_COORDS, DefaultModels.CUBE_NORMALS, i);
        data.objs.add(obj);
        return data ;
    }
}