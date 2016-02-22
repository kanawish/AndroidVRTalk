package com.kanawish.shaderlib.generation;

import com.kanawish.shaderlib.model.GeometryData;

import java.util.ArrayList;

import javax.inject.Inject;
import javax.inject.Singleton;

import timber.log.Timber;

/**
 * Created by ecaron on 15-11-25.
 */
@Singleton
public class BasicGenerator {
    @Inject
    public BasicGenerator() {
    }

    public GeometryData generateScene() {
        Timber.d("generateScene()...?1");
        Timber.d("buildCube()");
        GeometryData data = new GeometryData();
        data.objs = new ArrayList<>();
        float deg = (float) (Math.PI / 2f);
        GeometryData.Instanced i =
                new GeometryData.Instanced(1, new float[][]{{-4.0f, 0.0f, -15f}, {deg/2, deg/2, 0}, {1, 1, 1}, {1.0f, 0.8f, 0.3f, 1}, {1,0,0,0}});
        GeometryData.Obj obj = new GeometryData.Obj(DefaultModels.CUBE_COORDS, DefaultModels.CUBE_NORMALS, i);
        data.objs.add(obj);
        return data ;
//        return DefaultModels.buildCube();
    }
}
