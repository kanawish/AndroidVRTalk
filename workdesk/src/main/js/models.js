/**
* The Models module has a series of procedural model functions we use to demo live-coding concepts.
*/

//var Rx = require('rx');
var normal = require('get-plane-normal');

var models = module.exports.models = {};

var pusher = function (out, array) { for( var i in array ) out.push(array[i]); };

// Builds up the per-instance attributes: translation [vec3f], rotation [vec3f], scale [vec3f], material [vec3f]
// Basically a JSON GeometryData.Instanced
models.buildInstanced = function (count, translation, rotation, scale, color, params, mode)
{
    return {instancedCount: count, t: translation, r: rotation, s: scale, c: color, p:params, m: mode};
};

/**
 * The code that puts together our GeometryData.Instanced from the perInstance parameter.
 *
 * See: GeometryData.java
 */
models.buildInstancedFromArray = function (dataArray) {
    var t = [], r = [], s = [], c = [], p = [] ;
    for(var i in dataArray) {
        for( y in dataArray[i][0]) t.push(dataArray[i][0][y]) ;
        for( y in dataArray[i][1]) r.push(dataArray[i][1][y]) ;
        for( y in dataArray[i][2]) s.push(dataArray[i][2][y]) ;
        for( y in dataArray[i][3]) c.push(dataArray[i][3][y]) ;
        for( y in dataArray[i][4]) p.push(dataArray[i][4][y]) ;
    }
    return models.buildInstanced(dataArray.length,t,r,s,c,p);
};

// Build a JSON GeometryData.Obj
models.buildObj = function(vertices, normals, instanced) {
    return { v: vertices, n: normals, i: instanced };
};

models.buildPlane = function (instanced) {
    return {v: models.V_PLANE, n: models.N_PLANE, i: instanced};
};

models.buildCube = function (instanced) {
    return {v: models.V_CUBE, n: models.N_CUBE, i: instanced};
};

/*
    dims.xMax
        .yMax
    perlin lib

    TODO: Rx-if-y
 */
models.buildLandscape = function( instanced, dims, p ) {

    // Generate vertex grid
    var smooth = 25 ;
    var range = 3.5 ;
    var grid = [] ;
    for( var x = 0 ; x < dims.xMax ; x++ ) {
        grid.push([]);
        for( var y = 0 ; y < dims.yMax ; y++ ) {
            var h = p.noise.simplex2(x/smooth,y/smooth);
            // Build a grid of vertices with randomized heights.
            grid[x].push([x-(dims.xMax/2), h*range,y-(dims.yMax/2)]);
        }
    }

    var vertices = [];
    var normals = [];

    // Take the vertices, build a triangle mesh out of them.
    for (var x = 0; x < dims.xMax - 1; x++) {
        for (var y = 0; y < dims.yMax - 1; y++) {
            // Triangle A
            pusher(vertices,grid[x][y]);
            pusher(vertices,grid[x+1][y]);
            pusher(vertices,grid[x][y+1]);
            var normalA = normal([], grid[x][y], grid[x + 1][y], grid[x][y + 1]);
            // TODO: Invert the normal.
            pusher(normals, normalA);
            pusher(normals, normalA);
            pusher(normals, normalA);

            // Triangle B
            pusher(vertices,grid[x+1][y]);
            pusher(vertices,grid[x+1][y+1]);
            pusher(vertices,grid[x][y+1]);
            var normalB = normal([], grid[x + 1][y], grid[x + 1][y + 1], grid[x][y + 1]);
            pusher(normals, normalB);
            pusher(normals, normalB);
            pusher(normals, normalB);
        }
    }

    return {v: vertices, n: normals, i: instanced};
};

models.buildHouse = function() {
    var vertices = [] ;
    var normals = [] ;

    // A cube for the base


    // 2 planes for the roof

    // triangles for the corners
};

models.buildTriangle = function() {

};

// ***** CONSTANTS *****

// Plane vertices
models.V_PLANE = [
    // Front face
    -1.0, 1.0, 0.0,
    -1.0, -1.0, 0.0,
    1.0, 1.0, 0.0,
    -1.0, -1.0, 0.0,
    1.0, -1.0, 0.0,
    1.0, 1.0, 0.0
];

// Plane normals
models.N_PLANE = [
    // Front face
    0.0, 0.0, 1.0,
    0.0, 0.0, 1.0,
    0.0, 0.0, 1.0,
    0.0, 0.0, 1.0,
    0.0, 0.0, 1.0,
    0.0, 0.0, 1.0
];

// Cube vertices
models.V_CUBE = [
    // Front face
    -1.0, 1.0, 1.0,
    -1.0, -1.0, 1.0,
    1.0, 1.0, 1.0,
    -1.0, -1.0, 1.0,
    1.0, -1.0, 1.0,
    1.0, 1.0, 1.0,

    // Right face
    1.0, 1.0, 1.0,
    1.0, -1.0, 1.0,
    1.0, 1.0, -1.0,
    1.0, -1.0, 1.0,
    1.0, -1.0, -1.0,
    1.0, 1.0, -1.0,

    // Back face
    1.0, 1.0, -1.0,
    1.0, -1.0, -1.0,
    -1.0, 1.0, -1.0,
    1.0, -1.0, -1.0,
    -1.0, -1.0, -1.0,
    -1.0, 1.0, -1.0,

    // Left face
    -1.0, 1.0, -1.0,
    -1.0, -1.0, -1.0,
    -1.0, 1.0, 1.0,
    -1.0, -1.0, -1.0,
    -1.0, -1.0, 1.0,
    -1.0, 1.0, 1.0,

    // Top face
    -1.0, 1.0, -1.0,
    -1.0, 1.0, 1.0,
    1.0, 1.0, -1.0,
    -1.0, 1.0, 1.0,
    1.0, 1.0, 1.0,
    1.0, 1.0, -1.0,

    // Bottom face
    1.0, -1.0, -1.0,
    1.0, -1.0, 1.0,
    -1.0, -1.0, -1.0,
    1.0, -1.0, 1.0,
    -1.0, -1.0, 1.0,
    -1.0, -1.0, -1.0
];

// Cube normals
models.N_CUBE = [
    // Front face
    0.0, 0.0, 1.0,
    0.0, 0.0, 1.0,
    0.0, 0.0, 1.0,
    0.0, 0.0, 1.0,
    0.0, 0.0, 1.0,
    0.0, 0.0, 1.0,

    // Right face
    1.0, 0.0, 0.0,
    1.0, 0.0, 0.0,
    1.0, 0.0, 0.0,
    1.0, 0.0, 0.0,
    1.0, 0.0, 0.0,
    1.0, 0.0, 0.0,

    // Back face
    0.0, 0.0, -1.0,
    0.0, 0.0, -1.0,
    0.0, 0.0, -1.0,
    0.0, 0.0, -1.0,
    0.0, 0.0, -1.0,
    0.0, 0.0, -1.0,

    // Left face
    -1.0, 0.0, 0.0,
    -1.0, 0.0, 0.0,
    -1.0, 0.0, 0.0,
    -1.0, 0.0, 0.0,
    -1.0, 0.0, 0.0,
    -1.0, 0.0, 0.0,

    // Top face
    0.0, 1.0, 0.0,
    0.0, 1.0, 0.0,
    0.0, 1.0, 0.0,
    0.0, 1.0, 0.0,
    0.0, 1.0, 0.0,
    0.0, 1.0, 0.0,

    // Bottom face
    0.0, -1.0, 0.0,
    0.0, -1.0, 0.0,
    0.0, -1.0, 0.0,
    0.0, -1.0, 0.0,
    0.0, -1.0, 0.0,
    0.0, -1.0, 0.0
];
