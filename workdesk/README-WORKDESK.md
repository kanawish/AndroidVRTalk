# WorkDesk

The workdesk folder contains all you need to setup the "Publishing" part of the sample code.

## The tools

TODO: It would be great to eventually add a step-by-step to using gulp, setting up the node.js environment, etc. 

Meanwhile, the main pieces of the puzzle are [gulp.js](http://gulpjs.com/), [node.js](https://nodejs.org/en/), [Browserify](http://browserify.org/) and [Firebase](https://www.firebase.com/).

**node.js** is traditionally a server-side component, it gives us nice package and dependency management, as well as a system to include libraries cleanly in our javascript geometry generator. 

**browserify** converts our node geometry generator into a web-browser compatible javascript program.

**gulp.js** gives us a easy way to detect changes to any files in the project, start a build (only needed for the node geometry program), and pushes it out via `adb push` or a Firebase publishing call.

I personally use IntelliJ to work on the Javascript Geometry code, and Sublime with GLSL syntax highlighting to edit the vertex and fragment shaders.


## The concept

At it's core, this talk and this project is all about setting up a 'livecoding' environment, where you get instant feedback when prototyping a 3d scene. 

We use the Publish-Subscribe pattern. The Subscriber is the VR Viewer `/app` on the target mobile device(s). The Publisher is the javascript project `/workdesk`. 

`/workdesk` is a javascript project, but when modeling a scene, the client application expects 3 specific modeling programs. 

- **geo.js** Is executed on the mobile device, generates our 3d model data.
- **shader.vs** The vertex shader, a GLSL program, projects vertices on screen.
- **shader.fs** The fragment shader, the 'pixel painter' for on-screen primitives. 

`geo.js` is interpreted by an invisible webview. The `.vs` and `.fs` programs are interpreted by the client's OpenGL engine.

(Confused? Please refer to my [Talk @ Big Android BBQ 2015](https://youtu.be/bi4YTryqY-Q?list=PLWz5rJ2EKKc_HyE1QX9heAgTPdAMqc50z) for the deep-dive.)

The publisher is basically a gulp.js build script. [Gulp](http://gulpjs.com/) is a streaming build system. With gulp, we can setup a 'watch' on our 3d modeling programs. When any of these change, they are (if needed) compiled, then published to a messaging channel.

We currently supports 2 messaging channels. The first channel supported was Firebase. All you need for your own setup is to start a Firebase demo instance, and configure it in the gulp build file and the Android VR client. The second channel is via plain and simple `adb push`. The Android client watches for changes to the 3d modeling code on disk, and refreshes the scene when needed.

