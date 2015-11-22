/**
 * Created by kanawish on 15-08-05.
 */

var models = {} ;
exports.models = models ;

(function(m) {

    // Builds up the per-instance attributes: translation [vec3f], rotation [vec3f], scale [vec3f], material [vec3f]
    // Basically a JSON GeometryData.Instanced
    m.buildInstanced = function (count, translation, rotation, scale, color, params, mode)
    {
        return {instancedCount: count, t: translation, r: rotation, s: scale, c: color, p:params, m: mode};
    };

    /**
     * The code that puts together our GeometryData.Instanced from the perInstance parameter.
     *
     * See: GeometryData.java
     */
    m.buildInstancedFromArray = function (dataArray) {
        var t = [], r = [], s = [], c = [], p = [] ;
        for(var i in dataArray) {
            for( y in dataArray[i][0]) t.push(dataArray[i][0][y]) ;
            for( y in dataArray[i][1]) r.push(dataArray[i][1][y]) ;
            for( y in dataArray[i][2]) s.push(dataArray[i][2][y]) ;
            for( y in dataArray[i][3]) c.push(dataArray[i][3][y]) ;
            for( y in dataArray[i][4]) p.push(dataArray[i][4][y]) ;
        }
        return m.buildInstanced(dataArray.length,t,r,s,c,p);
    };

    // Build a JSON GeometryData.Obj
    m.buildObj = function(vertices, normals, instanced) {
        return { v: vertices, n: normals, i: instanced };
    };

    m.buildPlane = function (instanced) {
        return {v: m.V_PLANE, n: m.N_PLANE, i: instanced};
    };

    m.buildCube = function (instanced) {
        return {v: m.V_CUBE, n: m.N_CUBE, i: instanced};
    };

    /*
        dims.xMax
            .yMax
        perlin lib
     */
    m.buildField = function( instanced, dims, perlin ) {

        // Using Cube instance definitions to generate a simple cubic landscape
        perlin.noise.seed(Math.random());

        var grid = {} ;
        // Generate vertex grid
        for( var x = 0 ; x < dims.xMax ; x++ ) {
            grid.push({});
            for( var y = 0 ; y < dims.yMax ; y++ ) {
                var h = perlin.noise.simplex2(x/10,y/10);
                // Build a grid of vertices with randomized heights.
                grid[x].push({'x':x-(dims.xMax/2), 'y': h-6,'z':y-(dims.yMax/2)});

                //cubeInstanceDefs.push([[x-25, -6+(h*1.0), 25-y], [0, 0, 0], [0.5, 0.5, 0.5], [.6, 0.4, .0, 1], [3, 0, 0, 0]]);
            }
        }

        var mesh = {} ;
        // Take the vertices, build a triangle mesh out of them.
        for (var x = 0; x < dims.xMax - 1; x++) {
            for (var y = 0; y < dims.yMax - 1; y++) {
                grid[x][y];
                grid[x+1][y];
                grid[x][y+1];
            }
        }


    };

    // ***** CONSTANTS *****

    // Plane vertices
    m.V_PLANE = [
        // Front face
        -1.0, 1.0, 0.0,
        -1.0, -1.0, 0.0,
        1.0, 1.0, 0.0,
        -1.0, -1.0, 0.0,
        1.0, -1.0, 0.0,
        1.0, 1.0, 0.0
    ];

    // Plane normals
    m.N_PLANE = [
        // Front face
        0.0, 0.0, 1.0,
        0.0, 0.0, 1.0,
        0.0, 0.0, 1.0,
        0.0, 0.0, 1.0,
        0.0, 0.0, 1.0,
        0.0, 0.0, 1.0,
    ];

    // Cube vertices
    m.V_CUBE = [
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
    m.N_CUBE = [
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
    ]

})(models);