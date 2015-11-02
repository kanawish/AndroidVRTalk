package com.kanawish.shaderlib;

import com.kanawish.shaderlib.domain.CameraManager;
import com.kanawish.shaderlib.domain.PipelineProgramBus;

import dagger.Module;

/**
 * Created by ecaron on 15-10-28.
 */
// TODO: Should rename this to something else. We're not only dealing with shaders now.
@Module(
    injects = {
        CameraManager.class,
        PipelineProgramBus.class
    },
    complete = false,
    library = true
)
public class ShaderLibModule {

}
