package com.kanawish.androidvrtalk.domain;

import android.app.Application;
import android.os.Environment;
import android.os.FileObserver;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import javax.inject.Inject;
import javax.inject.Singleton;

import timber.log.Timber;

/**
 * The goal here is:
 * - Create a dir struct if needed
 * - Watch that folder for the creation of geo.js
 * - When file appears, FileObserver assignment
 * - Fallback? We have a default bundle.
 *
 * Created by ecaron on 15-11-11.
 */
@Singleton
public class FileSystemManager {

    private final Application app;

    private final FileObserver parentDirObserver;
    private final String filePath;

    private FileObserver fileObserver;

    @Inject
    public FileSystemManager(Application app) {
        this.app = app;

        File file = new File(app.getExternalFilesDir(null), "geo.js");
        filePath = file.getPath();
        boolean completed = file.getParentFile().mkdirs();

        parentDirObserver = new FileObserver(file.getParent()) {
            @Override
            public void onEvent(int event, String path) {
                if (event == FileObserver.CREATE && path.equals("geo.js")) {
                    watch();
                }
            }
        };
        parentDirObserver.startWatching();

        if( file.exists() ) watch();
    }

    private void watch() {
        fileObserver = new FileObserver(filePath) {
            @Override
            public void onEvent(int event, String path) {
                Timber.d("Event %d received for %s.", event, path);

                // TODO: Functionalize!
                if( event == FileObserver.MODIFY) {
                    try {
                        FileInputStream stream = app.openFileInput(filePath);
                    } catch (FileNotFoundException e) {
                        Timber.e(e, "Danger, Will Robinson.");
                    }
                }
            }
        };

        fileObserver.startWatching();
    }

    private boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            return true;
        }
        return false;
    }
}
