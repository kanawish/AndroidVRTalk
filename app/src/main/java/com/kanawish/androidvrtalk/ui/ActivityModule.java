package com.kanawish.androidvrtalk.ui;

import com.kanawish.androidvrtalk.AppModule;
import com.kanawish.androidvrtalk.domain.DomainModule;

import dagger.Module;

/**
 * Created by ecaron on 15-10-28.
 */
@Module(
        addsTo = AppModule.class,
        injects = {} // Views, activity specific items, see u2020 for example
)
public class ActivityModule {
    private final VrTalkActivity vrTalkActivity;

    public ActivityModule(VrTalkActivity vrTalkActivity) {
        this.vrTalkActivity = vrTalkActivity;
    }

    // TODO: This is just a shell module to build upon. Add as we go.
}
