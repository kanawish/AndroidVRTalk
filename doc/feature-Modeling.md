# Feature - Modeling

## Goal

Inspired by a Gamasutra Article, [Modelling by numbers](http://www.gamasutra.com/blogs/JayelindaSuridge/20130903/199457/Modelling_by_numbers_Part_One_A.php)

The goal here is to follow the article and build up a small model building library. 

### Changes to the Geometry class

We don't support multiple instances of GeometryData.Obj right now, and we really should.

I'm unclear on how things are done say if we draw things "out of order" in a scene (background vs foreground objects). Keep that in mind.

GeometryRenderer + GeometryInstance are the 2 new classes where I'll try to implement multi-instanced draw calls. This is the first necessary piece to building serious scenes and using proc-gen for models.

### The Plane



... 