package com.kanawish.androidvrtalk.ui;

import dagger.Module;

/**
 *
 */
@Module(
    injects = {
        VrTalkActivity.class,
        PlainGLActivity.class
    },
    complete = false,
    library = true
)
public class UiModule {

}
