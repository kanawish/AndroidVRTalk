package com.kanawish.androidvrtalk.domain;

import com.kanawish.androidvrtalk.ui.VrTalkActivity;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

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

    // Pick of Firebase/FileSystem manager, depending on your needs.
    @Provides @Singleton ScriptManager provideScriptManager(FileSystemManager manager) { return manager; }

}
