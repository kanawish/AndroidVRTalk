/**
 * Looked into this following the hints of colleagues
 * and this article: http://www.100percentjs.com/just-like-grunt-gulp-browserify-now/
 *
 * Created by ecaron on 15-10-26.
 */
'use strict';

var browserify = require('browserify');
var gulp = require('gulp');
var del = require('del');
var shell = require('gulp-shell');
var runSequence = require('run-sequence');
var source = require('vinyl-source-stream');
var buffer = require('vinyl-buffer');
var gutil = require('gulp-util');
var sourcemaps = require('gulp-sourcemaps');
var assign = require('lodash.assign');

// Shipping specific dependencies
var Firebase = require("firebase");
var fs = require('fs');

// add custom browserify options here
var customOpts = {
    entries: ['./src/main/js/main.js'],
    debug: true
};
var opts = assign({}, customOpts);

var b = browserify(opts);


gulp.task('buildJs', bundle); // so you can run `gulp js` to build the file
b.on('update', bundle); // on any dep update, runs the bundler
b.on('log', gutil.log); // output build logs to terminal
function bundle() {
    return b.bundle()
        // log errors if they happen
        .on('error', gutil.log.bind(gutil, 'Browserify Error'))
        .pipe(source('bundle.js'))
        // optional, remove if you don't need to buffer file contents
        .pipe(buffer())
        // optional, remove if you dont want sourcemaps
        .pipe(sourcemaps.init({loadMaps: true})) // loads map from browserify file
        // Add transformation tasks to the pipeline here.
        .pipe(sourcemaps.write('./')) // writes .map file
        .pipe(gulp.dest('./dist'));
}


var fbRef = new Firebase('https://YOUR-FIREBASE-INSTANCE-GOES-HERE');

gulp.task('shipGeoFb', function (done) {
    // https://docs.nodejitsu.com/articles/file-system/how-to-read-files-in-nodejs
    fs.readFile('dist/bundle.js', 'utf8', function (err, data) {
        if (err) {
            console.log(err);
            done();
        }
        var geo_script = {code: data};

        fbRef.child('geo_script').set(geo_script, function () {
            gutil.log("fbRef.set() completed.");
            done();
        });
    });
});

gulp.task('clean', function (done) {
    del(['dist/*'], done);
    done();
});

gulp.task('cleanGeo', function (done) {
    del(['dist/bundle.*'], done);
    done();
});

gulp.task('cleanGlsl', function (done) {
    del(['dist/shader.*'], done);
    done();
});

gulp.task('buildGlsl', function() {
    return gulp.src('src/main/glsl/shader.*').pipe(gulp.dest('./dist/'));

});

gulp.task('shipGeoAdb', shell.task([
    'echo "adb push dist/bundle.js  sdcard/Android/data/com.kanawish.androidvrtalk/files/geo.js"',
    'adb push dist/bundle.js  sdcard/Android/data/com.kanawish.androidvrtalk/files/geo.js'
]));

gulp.task('shipVertexShaderAdb', shell.task([
    'adb push dist/shader.vs sdcard/Android/data/com.kanawish.androidvrtalk/files/shader.vs'
]));

gulp.task('shipFragmentShaderAdb', shell.task([
    'adb push dist/shader.fs sdcard/Android/data/com.kanawish.androidvrtalk/files/shader.fs'
]));

// TODO: Needs validating.
gulp.task('shipVertexShaderFb', function (done) {
    // https://docs.nodejitsu.com/articles/file-system/how-to-read-files-in-nodejs
    fs.readFile('dist/shader.vs', 'utf8', function (err, vertexShaderCode) {
        if (err) {
            console.log(err);
            done();
        }
        var vertex_script = {code: vertexShaderCode};
        fbRef.child('vertex_shader').set(vertex_script, function () {
            gutil.log("glsl fbRef.set(vs) completed.");
            done();
        });
    });
});

// TODO: Needs validating.
gulp.task('shipFragmentShaderFb', function (done) {
    fs.readFile('dist/shader.fs', 'utf8', function (err, fragmentShaderCode) {
        if (err) {
            console.log(err);
            done();
        }
        var fragment_script = {code: fragmentShaderCode};
        fbRef.child('fragment_shader').set(fragment_script, function () {
            gutil.log("glsl fbRef.set(fs) completed.");
            done();
        });
    });
});

// NOTES:
// http://www.100percentjs.com/just-like-grunt-gulp-browserify-now/
// https://gist.github.com/Sigmus/9253068

gulp.task('fullJs', function(done) {
    runSequence('cleanGeo','buildJs', 'shipGeoAdb', done);
});

gulp.task('fullGlsl', function(done) {
    runSequence('cleanGlsl','buildGlsl','shipVertexShaderAdb','shipFragmentShaderAdb',done);
});

gulp.task('default', ['fullJs','fullGlsl'], function (done) {
    // Under IntelliJ we don't need a debouncer, source autosave can be configured as we like.
    gulp.watch('src/main/js/**', ['fullJs']);
    gulp.watch('src/main/glsl/**', ['fullGlsl']);
    done();
});

