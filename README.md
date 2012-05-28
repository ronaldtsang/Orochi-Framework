Orochi-Framework (Version 1.0)
==================================================

Orochi-Framwork is a:
--------------------------------------
Cross platform framework for building HTML5 native app

* version 1.0 supported devices type: android 2.1 or higher
* will add iOS support in version 2.0


Try the demo:
--------------------------------------
### [Full-Function Testing Tool](https://github.com/ronaldtsang/Orochi-Framework/blob/master/android/orochi_source.apk) ###
webapp link: http://orochis-den.com/orochi_jqmdemo/app.html
* To close the application: you must turn off the "Native Service" first, else the "Native Service" will keep running.
* You may type in your webapp link for testing


### [Simple Example](https://github.com/ronaldtsang/Orochi-Framework/blob/master/android/orochi_example.apk) ###
webapp link: http://orochis-den.com/orochi_jqmdemo/app.html?serviceName=Orochi-example
* "Native Service" will be closed automatically when you close the application.
* Webapp link is hardcoded.


### For both demo ###
You may find the demo webapp source [here](https://github.com/downloads/ronaldtsang/Orochi-Framework/orochi_1.0_web_example_with_source.zip).
* this framework is usable without jqmobile, but jquery is a must.

After you start the demo application(with "Native Service" on), your may:
* open the "webapp link" on your mobile's browser
* open the "webapp link" on your desktop's() browser with extra "GET Parameter": ?serviceIP=192.168.1.x (192.168.1.x is your mobile's IP)(only work on local network)


How to start?
--------------------------------------
1. Download all [javascript files](https://github.com/downloads/ronaldtsang/Orochi-Framework/orochi_1.0_js.zip)

2. Include the following lines in you webapp:
`<script src="jqmobile/jquery.min.js"></script>`
`<script type="text/javascript" src="js/weboo/weboo.js"></script>`
`<script type="text/javascript" src="js/orochi/orochi.js"></script>`
`<script type="text/javascript" src="js/orochi/orochi.ext.js"></script>`

3. Setup your own offline.manifest if you want to have an offline accessible webapp.

4. Download the [example source code](https://github.com/downloads/ronaldtsang/Orochi-Framework/orochi_1.0_android_orochi_example.zip) and modify it to suit your needs. (or you may test it with the "Full-Function Testing Tool" first)

5. change the Hardcoded link in "orochi.example.MainActivity" line 39. 
`private String urlAddress = "http://orochis-den.com/orochi_jqmdemo/app.html?serviceName=Orochi-example";`

6. Run and have fun...


Advance
--------------------------------------
To build your own Native API:
1. Take a look at the native package [orochi.nativeadapter.RequestHandler](https://github.com/ronaldtsang/Orochi-Framework/tree/master/android/orochi_source/src/orochi/nativeadapter/requesthandler) 
2. Take a look at the [javascript files](https://github.com/ronaldtsang/Orochi-Framework/tree/master/web_example_with_source/source_js/orochi)(But not the "orochi.js")
3. I think you should be able to make it XD.
4. Please send it to me, if you think your API is useful to others.


Simple Javascript API:
--------------------------------------
[Orochi Framework 1.0 - JS API](https://docs.google.com/document/d/1Rlwf2HBYCG4AafVFCEpuifXhBJuqy3IRdk7cix9ZWe0/edit)


The Source Code insludes:
--------------------------------------
1. [JSON in Java](http://www.json.org/java/)
2. [jQuery](http://jquery.com/)
3. [jQuery Mobile](http://jquerymobile.com/)


Questions?
--------------------------------------
email me: ronaldtsang@orochis-den.com


