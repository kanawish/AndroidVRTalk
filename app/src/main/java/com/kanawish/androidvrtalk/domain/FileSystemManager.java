package com.kanawish.androidvrtalk.domain;

import android.app.Application;
import android.os.FileObserver;

import com.kanawish.shaderlib.domain.PipelineProgramBus;
import com.kanawish.shaderlib.utils.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;

import javax.inject.Inject;
import javax.inject.Singleton;

import timber.log.Timber;

/**
 * The goal here is:
 * - Create a dir struct if needed
 * - Watch that folder for the creation of geo.js
 * - When file appears, observe it.
 * - If file actually exists, observe it.
 * - Fallback? We have a default bundle.
 *
 * Created by ecaron on 15-11-11.
 */
@Singleton
public class FileSystemManager implements ScriptManager {

    public static final String GEO_JS = "geo.js";
    public static final String SHADER_VS = "shader.vs";
    public static final String SHADER_FS = "shader.fs";

    private final Application app;
    private final PipelineProgramBus bus;

    private final FileObserver parentDirObserver;

    private final String geoFilePath;
    private FileObserver geoFileObserver;

    private final String vsFilePath;
    private FileObserver vsFileObserver;

    private final String fsFilePath;
    private FileObserver fsFileObserver;

    @Inject
    public FileSystemManager(Application app, PipelineProgramBus bus) {
        this.app = app;
        this.bus = bus;

        File file = new File(app.getExternalFilesDir(null), GEO_JS);
        geoFilePath = file.getPath();
        if( file.exists() ) geoFileObserver = watch(geoFilePath, bus::publishGeoScript);

        file = new File(app.getExternalFilesDir(null), SHADER_VS);
        vsFilePath = file.getPath();
        if( file.exists() ) vsFileObserver = watch(vsFilePath, bus::publishVertexShader);

        file = new File(app.getExternalFilesDir(null), SHADER_FS);
        fsFilePath = file.getPath();
        if( file.exists() ) fsFileObserver = watch(fsFilePath, bus::publishFragmentShader);

        // Creates the parent folder, might do nothing if already exists.
        boolean completed = file.getParentFile().mkdirs();

        parentDirObserver = new FileObserver(file.getParent()) {
            @Override
            public void onEvent(int event, String path) {
                if (event == FileObserver.CREATE || event == FileObserver.CLOSE_WRITE) {
                    if(path.equals(GEO_JS)) {
                        if(geoFileObserver!=null) geoFileObserver.stopWatching();
                        geoFileObserver = watch(geoFilePath, bus::publishGeoScript);
                    } else if(path.equals(SHADER_VS)) {
                        if(vsFileObserver!=null) vsFileObserver.stopWatching();
                        vsFileObserver = watch(vsFilePath, bus::publishVertexShader);
                    } else if(path.equals(SHADER_FS)) {
                        if(fsFileObserver!=null) fsFileObserver.stopWatching();
                        fsFileObserver = watch(fsFilePath, bus::publishFragmentShader);
                    }
                }
            }
        };
        parentDirObserver.startWatching();
    }

    private interface ScriptProcessor {
        void processScript(String script) ;
    }

    /**
     * Watch a given file for changes. Note that if you let that instance get garbage collected,
     * it will be collected and will stop observing immediately.
     *
     * @return a watched file observer.
     */
    private FileObserver watch(String filePath, ScriptProcessor processor) {
        processFile(filePath, processor);

        // NOTE: In fact, this will rarely be used, since an `adb push` command yields a "CREATE".
        FileObserver fileObserver = new FileObserver(filePath) {
            @Override
            public void onEvent(int event, String path) {

                // TODO: Functionalize?
                // When the file is modified, we'll want to read it, and emit the content.
                if( event == FileObserver.MODIFY ) {
                    processFile(filePath, processor);
                }

                // NOTE: When file gets deleted, observer will stop itself.
            }
        };

        fileObserver.startWatching();
        return fileObserver;
    }

    private void processFile(String filePath, ScriptProcessor processor) {
        Timber.d("Processing %s.", filePath);
        try {
            InputStreamReader isr = new InputStreamReader(new FileInputStream(new File(filePath)));
            String content = IOUtils.readFile(isr);
            // Quick-fix, added this check since I was getting events I did not expect (create empty, etc)
            if( content != null && content.length() > 0 ) processor.processScript(content);
        } catch (FileNotFoundException e) {
            Timber.e(e, "Danger, Will Robinson.");
        } catch (IOException e) {
            Timber.e(e, "Danger, Will Robinson.");
        }
    }


}
