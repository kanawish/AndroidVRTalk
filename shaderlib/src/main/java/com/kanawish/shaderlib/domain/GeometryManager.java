package com.kanawish.shaderlib.domain;

import com.kanawish.shaderlib.model.GeometryData;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.ScriptableObject;

import timber.log.Timber;

/**
 * Created by ecaron on 15-10-25.
 */
public class GeometryManager {

    // See for notes http://www.skyscanner.net/blogs/developing-mobile-cross-platform-library-part-3-javascript
    // http://openaphid.github.io/blog/2013/01/17/part-i-how-to-choose-a-javascript-engine-for-ios-and-android-apps/
    public static final GeometryData generateGeometryData(String jsString) {
        Context rhinoContext = Context.enter();
        // http://stackoverflow.com/questions/3859305/problems-using-rhino-on-android
        rhinoContext.setOptimizationLevel(-1);
        ScriptableObject scope = rhinoContext.initStandardObjects();
        Object result = rhinoContext.evaluateString(scope, jsString, "<cmd>", 1, null);
        Timber.d("generateGeometryData done.");
        return null;
    }
}
