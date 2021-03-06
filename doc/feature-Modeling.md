# Feature - Modeling

## Goal

Inspired by a Gamasutra Article, [Modelling by numbers](http://www.gamasutra.com/blogs/JayelindaSuridge/20130903/199457/Modelling_by_numbers_Part_One_A.php)

The goal here is to follow the article and build up a small model building library. 

## Worklog 

- [ ] Following GS article, add more modeling:
	- [ ] Height mapped plain
	- [ ] Houses (boxes + plains + triangles)
	- [ ] Fence
	- [ ] Introduce RxJs to add noise to builds.
	- [ ] Map things to stick to height map.
- [x] Fixed up the multi model support. 15/11/22 
- [x] ~~Check if 'draw order' is important~~ No, it is not. (Makes sense)
- ...

## Backlog



### Changes to the Geometry class

Added support for multiple instances of GeometryData.Obj.

Right now I'm unclear on how things are done say if we draw things "out of order" in a scene (background vs foreground objects). 

GeometryRenderer + GeometryInstance are the 2 new classes where I'll try to implement multi-instanced draw calls. This is the first necessary piece to building serious scenes and using proc-gen for models.

### The Plane


... 