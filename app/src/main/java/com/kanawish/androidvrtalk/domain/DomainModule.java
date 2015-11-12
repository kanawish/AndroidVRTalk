package com.kanawish.androidvrtalk.domain;

import com.kanawish.androidvrtalk.ui.VrTalkActivity;

import dagger.Module;

/**
 *
 */
@Module(
    injects = {
        FirebaseManager.class,
        FileSystemManager.class
    },
    complete = false,
    library = true
)
public class DomainModule {


}
