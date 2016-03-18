/**
 *
 * Goal: The main script is to be executed client side, and generate the scene data on-device.
 *
 * This means we bundle everything here using browserify, and push updates to our client devices,
 * that will then run this on a Javascript interpreter. (After evaluating Rhino and Duktape, went
 * back to Chrome in a hidden WebView, for npm lib support reasons.)
 *
 * Created by kanawish on 15-08-04.
 *
 */

// REQUIRE DEPENDENCIES

var m = require("./models.js").models;
var p = require("./perlin.js").perlin;

//var Rx = require("rx");

var t = require("./test.js");
t.test();


var sprintf = require("sprintf-js").sprintf,
    vsprintf = require("sprintf-js").vsprintf;

//var Firebase = require("firebase");
//var Bacon = require("baconjs");
//var $ = require("jquery");

//var Rx = require("./rx.compat.js");

// LOCAL FUNCTIONS

// Shorthand function for console logging.
var cl = function (l) {
    console.log(l)
};


// Reminder: 360º == 2π
var rot = 0.25*3.1415; // 45º

// Series of Cube instance definitions
var cubeInstanceDefs = [
    //[[0.0, 0.0, -15], [rot, rot, 0], [1, 1, 1], [1.0, 1.0, 1.0, 1], [1,0,0,0]], // start cube
    [[-4.0, 0.0, -15], [rot, rot, 0], [1, 1, 1], [1.0, 0.1, 0.0, 1], [1,0,0,0]], // red cube
    [[4.0, 0.0, -15], [rot, rot, 0], [1, 1, 1], [1.0, 0.0, 1.0, 1], [1,0,0,0]], // purple cube
    //[[0, -3, -7], [0, 0, 0], [2, 0.25, 1.0], [1., 0.5, 0., 1 ], [1,0,0,0]], // orange "floor"
    [[1.5, -1.5, -10], [rot, 0, 0], [1, 0.5, 1], [0, 0.8, 0, 1], [1,0,0,0]], // green box
    [[-4, 0.0, 10], [0, rot, rot], [1, 1, 1], [1, 0.9, 0, 1], [1,0,0,0]], // yellow box, top right
    [[4, 0.0, 10], [0, rot, rot], [1, 1, 1], [0., 0.9, 1.0, 1], [1,0,0,0]], // cyan cube
    //[[-1.25, 0.5, -10], [0, 0, 0], [1.0,1.0,1.0], [0, 0.6, 1, 1], [5,0,0,0]], // 'raymarching-box'
];


// Using Cube instance definitions to generate a simple cubic landscape
/*
p.noise.seed(0.42);
for( var x = 0 ; x < 50 ; x++ ) {
    for( var y = 0 ; y < 50 ; y++ ) {
        var h = p.noise.simplex2(x/10,y/10);
        cubeInstanceDefs.push([[x-25, -6+(h*1.0), 25-y], [0, 0, 0], [0.5, 0.5, 0.5], [.6, 0.4, .0, 1], [3, 0, 0, 0]]);
    }
}
*/

// Using Cube instance defs to create a Skybox (Note the fragment shader parameter, type '2')
cubeInstanceDefs.push([[0, 1, -50], [0, 0, 0], [50, 50,.5], [0, .99, .99, 1], [2,0,0,0]]);
cubeInstanceDefs.push([[50, 1, 0], [0, rot*2, 0], [50, 50,.5], [0, .99, .99, 1], [2,0,0,0]]);
cubeInstanceDefs.push([[-50, 1, 0], [0, rot*-2, 0], [50, 50,.5], [0, .99, .99, 1], [2,0,0,0]]);
cubeInstanceDefs.push([[0, 1, 50], [0, 0, 0], [50, 50,.5], [0, .99, .99, 1], [2,0,0,0]]);
cubeInstanceDefs.push([[0, 51, 0], [rot*2, 0, 0], [50, 50,.5], [0, .99, .99, 1], [2,0,0,0]]);
cubeInstanceDefs.push([[0, -49, 0], [rot*-2, 0, 0], [50, 50,.5], [0, .99, .99, 1], [2,0,0,0]]);

var cubeInstanced = m.buildInstancedFromArray(cubeInstanceDefs);

var planeInstanced = m.buildInstancedFromArray( [
    [[-0.5, -1.0, -12], [0, rot*1.2, rot *.5], [1, 1, 1], [1.0, 0.3, 0.0, 1], [1,0,0,0]],
    //[[-1.6, -2.5, -8], [rot*1.3, 0, 0], [1, 1, 1], [1.0, 0.3, 0.0, 1], [1,0,0,0]]
] );

//debugger ;

// Using Cube instance definitions to generate a simple cubic landscape
p.noise.seed(0.42);

var target = {objs: [
    //{v: m.V_PLANE, n: m.N_PLANE, i: planeInsts},
    //{v: m.V_CUBE, n: m.N_CUBE, i: instances},
    m.buildCube(cubeInstanced),
    m.buildPlane(planeInstanced),
    m.buildLandscape(
        m.buildInstancedFromArray([ [[0, -4.25, -10], [0, 0, 0], [1,1,1], [.5, .99, .4, 1], [1,0,0,0]] ] ),
        {xMax:30,yMax:30}, p)
    //{v: m.V_CUBE, n: m.N_CUBE, i: instances}
]};


// This is where we 'export' our 'main()' to our Rhino setup on the mobile side.
//self.main = function()  { return JSON.stringify(target); } // stringify() needed for duktape, perhaps rhino as well.
self.main = function()  { return target; }

