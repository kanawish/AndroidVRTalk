package com.kanawish.androidvrtalk.ui;

import android.app.Activity;

import com.kanawish.androidvrtalk.AppModule;

import dagger.Module;

/**
 * Created by ecaron on 15-10-28.
 */
@Module(
        addsTo = AppModule.class,
        injects = {} // Views, activity specific items, see u2020 for example
)
public class ActivityModule {
    private final Activity currentActivity;

    public ActivityModule(Activity currentActivity) {
        this.currentActivity = currentActivity;
    }

    // TODO: This is just a shell module to build upon. Add as we go.
}
