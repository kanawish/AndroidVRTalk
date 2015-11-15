package com.kanawish.androidvrtalk;

import android.app.Application;

import com.kanawish.androidvrtalk.domain.FirebaseManager;
import com.kanawish.androidvrtalk.ui.UiModule;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module(
        includes = {
            UiModule.class,
//        DataModule.class
        },
        injects = {
            VrTalkApp.class,
        }
)
public final class AppModule {
    private final VrTalkApp app;

    public AppModule(VrTalkApp app) {
        this.app = app;
    }

    @Provides @Singleton Application provideApplication() {
        return app;
    }

    @Provides @Singleton ActivityHierarchyServer provideActivityHierarchyServer() {
        return ActivityHierarchyServer.NONE;
    }


}
