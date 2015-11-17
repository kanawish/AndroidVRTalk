# Feature

Problem: Firebase is a great component for prototyping the live-coding features of the app, but there's a few key drawbacks.

- Need for internet connectivity.
- Proprietary.
- Latency. (somewhat)

So, I wanted to explore using WiFi Direct and WebSockets. The two might not really be compatible, but both approaches seem interesting.

Further experiments ended up leading to adding offline ADB upload support, which is quick enough but also suffers from some latency.

## Roadmap: 
- [ ] Add Nearby pub-sub capability
	- [ ] gulp.watch -> adb -> Nearby pub-sub
- [ ] Add VRWorkDesk into project
	- [ ] Make it runnable from the command line
	- [ ] Write a HOW-TO (npm setup/install)
	- [ ] Remove all credentials from that copy.
- [x] Realtime *scripting* over Firebase
	- [x] gulp + Browserify + gulp.watch
	- [x] Rhino client execution
	- [x] Upgrade to Chrome v8

## Research links

- [http://developer.android.com/reference/android/net/wifi/p2p/package-summary.html](http://developer.android.com/reference/android/net/wifi/p2p/package-summary.html)
- [http://developer.android.com/guide/topics/connectivity/wifip2p.html](http://developer.android.com/guide/topics/connectivity/wifip2p.html)
- [reverse tethering wifi direct phone to laptop](http://androidforums.com/threads/howto-wifi-direct-use-your-laptop-desktop-as-a-softap-for-android-reverse-tethering.552970/)
- [http://browserify.org/](http://browserify.org/)
- [https://www.npmjs.com/package/watchify](https://www.npmjs.com/package/watchify)
- [https://developers.google.com/nearby/connections/android/send-messages](https://developers.google.com/nearby/connections/android/send-messages)

## Game plan...

### Play services multiplayer game lib
Limited to 8 players, so can't do a massive mesh... but likely could have uses.

### Nearby
Actually Nearby looks perfect for our use case. Just need to check if we're required to have internet, it doesn't look like it, we just need wifi if I read this right... could be wrong.

### Rhino + Browserify 
Looks like it's possible to run the geometry building javascript on the device. That opens up some nice possibilities, we could imagine live updates of all kinds at that time. Using also watchify and other js build tools, seems reasonable to think we could even add new 'requires' OTA. 

### Chrome V8 + Browserify?
**UPDATE 15/10/30** - Rhino is not working that great, it doesn't like Rx, or anything fancy like the Firebase plugin, etc. 

At first I figured I might do without using Chrome as a js interpreter, but it looks like it'll give much better perf, so I'll put vinegar in my wine ;-) and try to use it, the perf will likely be better.

### Updating over WiFi Direct
Could be interesting to avoid needing a wifi hub, but, it's easy enough to join an open wifi network.. it could be good for an eventual production app, to eliminate the 'let me on your wifi' friction points for multiplayer.

NOTE: *Robert's idea here for easy wifi password sharing, this could enable it too.*

### WebSockets over standard local Wifi
See about pushing directly content to the devices, allowing to avoid Firebase for conference settings. (At least have an alternative...)

NOTE:  *Actually, why bother? Nearby should be fine*


## Final idea

- WebSockets [for local-only Wifi]
	- 'Nearby' connected device(s)
	- Local Node.js websocket server 
	- IntelliJ js publisher
	- Chrome shader publishers
	- Nearby clients <-> Nearby host <-> node ws server <-> publishers
- Firebase [over Internet] 
	- Viewer subscribers
	- IntelliJ js publisher
	- Chrome shader publishers
	- Firebase

Big difference now will be that we publish the javascript program for the geometry, instead of publishing the geometry data itself. That part should be possible thanks to Rhino + Browserify, and should become realtime thanks to Watchify.

## Further notes and ideas

I believe it would be relatively trivial to have our Ace-based web editors push a `.js` into Firebase, have a node.js process running in our local IntelliJ that listens for these updates, saves the file changes to disk, triggering gulp.watch that would browserify the project, re-send to Firebase in the right target node, and publish to our remote viewers.

One thought here, one of the big issues so far has been to find a workflow for versioning the web shaders edited in the web-pane. It would be easy to adapt the approach above, perhaps even adding a small shader naming box to the web page.



