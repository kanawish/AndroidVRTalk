package com.kanawish.shaderlib.utils;

import android.content.Context;
import android.content.res.Configuration;
import android.view.Surface;
import android.view.WindowManager;

/**

 Orientation detection

 http://stackoverflow.com/questions/4727800/detect-android-orientation-landscape-left-v-landscape-right
 http://stackoverflow.com/questions/5088856/how-to-detect-landscape-left-normal-vs-landscape-right-reverse-with-support

 */
public class OrientationUtils {

    public static int getDeviceDefaultOrientation(Context c) {
        Configuration config = c.getResources().getConfiguration();

        WindowManager windowManager =  (WindowManager) c.getSystemService(Context.WINDOW_SERVICE);
        int rotation = windowManager.getDefaultDisplay().getRotation();

        if (isDefaultLandscape(config, rotation)) {
            return Configuration.ORIENTATION_LANDSCAPE;
        } else {
            return Configuration.ORIENTATION_PORTRAIT;
        }
    }

    // Should be good for phone or tablet.
    public static boolean isReverseLandscape(Context c) {
        Configuration config = c.getResources().getConfiguration();

        WindowManager windowManager =  (WindowManager) c.getSystemService(Context.WINDOW_SERVICE);
        int rotation = windowManager.getDefaultDisplay().getRotation();

        if( isDefaultLandscape(config,rotation)) {
            return rotation == Surface.ROTATION_180;
        } else {
            return rotation == Surface.ROTATION_270;
        }
    }

    private static boolean isDefaultLandscape(Configuration config, int rotation) {
        return ((rotation == Surface.ROTATION_0 || rotation == Surface.ROTATION_180) &&
                config.orientation == Configuration.ORIENTATION_LANDSCAPE) ||
                ((rotation == Surface.ROTATION_90 || rotation == Surface.ROTATION_270) &&
                        config.orientation == Configuration.ORIENTATION_PORTRAIT);
    }

}