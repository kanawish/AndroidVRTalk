package com.kanawish.shaderlib.domain;

//import android.content.Context;
import android.app.Application;
import android.content.Context;
import android.webkit.WebSettings;
import android.webkit.WebView;

import com.kanawish.shaderlib.model.GeometryData;
import com.squareup.duktape.Duktape;
import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;

import org.mozilla.javascript.ScriptableObject;

import java.io.IOException;

import javax.inject.Inject;
import javax.inject.Singleton;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import timber.log.Timber;

/**
 * Created by ecaron on 15-10-25.
 */
@Singleton
public class GeometryManager {

    private static final Moshi MOSHI = new Moshi.Builder().build();

    private WebView webView;

    @Inject
    public GeometryManager(Application appContext) {
        initWebview(appContext);
    }

    // TODO: Implement, use injection, etc.
    public final void initWebview(Context context) {
        // We want Chrome dev-tools debugging capabilities.
        WebView.setWebContentsDebuggingEnabled(true);

        webView = new WebView(context);

        // Optional
//        webView.setWebViewClient(new WebViewClient());
//        webView.setWebChromeClient(new WebChromeClient());
        // TODO: Implement
//        webView.addJavascriptInterface(interfaceInstance, "android");
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);

        // For now we leave this empty, we'll be running full browserified builds in the geo generation step.
        webView.loadUrl("file://android_asset/html/empty.html"); // file://android_asset/index.html

        // NOTE: Bottom line, using this approach, js/node devs would likely feel very much at home with our setup.
    }

/*
        GeometryData parsedData ;

        Observable
            .create((Observable.OnSubscribe<String>) subscriber -> {
                webView.evaluateJavascript(script, value -> { subscriber.onNext(value); });
            })
            .subscribeOn(AndroidSchedulers.mainThread())
            .first()
            .subscribe(
                s -> Timber.d("Result %s", s),
                throwable -> Timber.e(throwable,"Error")
            );

        return duktapeGeometryData(script);
*/
    public final GeometryData webviewGeometryData(final String script) {

        GeometryData geometryData =
            Observable
                .create((Observable.OnSubscribe<String>) subscriber -> {
                    webView.evaluateJavascript(script, value -> { subscriber.onNext(value); }); // No need to stringify() here...
                })
                .subscribeOn(AndroidSchedulers.mainThread())
                .observeOn(Schedulers.newThread())
                .map(geoDataJson -> parseGeometryDataJson(geoDataJson))
                .first()
                .doOnError(throwable -> Timber.e(throwable, "Processing error."))
                .toBlocking()
                .first();

        return geometryData;

    }

    // See for notes http://www.skyscanner.net/blogs/developing-mobile-cross-platform-library-part-3-javascript
    // http://openaphid.github.io/blog/2013/01/17/part-i-how-to-choose-a-javascript-engine-for-ios-and-android-apps/
    public static final GeometryData rhinoGeometryData(String jsString) {
        Timber.d("Running on thread %s", Thread.currentThread().getName());
        // Execute the script using Rhino.
        org.mozilla.javascript.Context rhinoContext = org.mozilla.javascript.Context.enter();
        // http://stackoverflow.com/questions/3859305/problems-using-rhino-on-android
        rhinoContext.setOptimizationLevel(-1);
        ScriptableObject scope = rhinoContext.initStandardObjects();
        String result = (String) rhinoContext.evaluateString(scope, jsString, "<cmd>", 1, null);
        return parseGeometryDataJson(result);
    }

    public static final GeometryData duktapeGeometryData(String jsString) {
        Timber.d("Running on thread %s", Thread.currentThread().getName());
        Duktape duktape = Duktape.create();

        try {
            String result = duktape.evaluate(jsString);
            Moshi moshi = new Moshi.Builder().build();
            JsonAdapter<GeometryData> jsonAdapter = moshi.adapter(GeometryData.class);
            return jsonAdapter.fromJson(result);
        } catch (Exception e) {
            Timber.e(e,"Error while evaluating javascript.");
        } finally {
            duktape.close();
        }

        return null ;
    }

    private static GeometryData parseGeometryDataJson(String result) {
        // Use Moshi to parse the data.
        Timber.d("parseGeometryDataJson() called with");
        Timber.d("%s", result);
        JsonAdapter<GeometryData> jsonAdapter = MOSHI.adapter(GeometryData.class);
        try {
            return jsonAdapter.fromJson(result);
        } catch (IOException e) {
            throw new RuntimeException("Failed to parse results into a GeometryData format.");
        }
    }

}
