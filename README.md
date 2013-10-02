MongoDB Synchronous Scala Driver (mssd)
=======================================

MongoDB Synchronous Scala Driver (mssd) is based on the plain old legacy [mongodb java driver](http://docs.mongodb.org/ecosystem/drivers/java/) and adds some Scala beauty to it.
It's similar to [casbash](http://mongodb.github.io/casbah/) but with a different api which was inspired by the [Play2 JSON API](http://www.playframework.com/documentation/2.2.0/ScalaJson) for the bson documents and by the [mongodb-async-driver](http://www.allanbank.com/mongodb-async-driver/index.html) for it's general API.

This driver is work in progress and not complete yet. Please feel free to contribute. It is used in production at [WebPageAnalyse](http://www.webpageanalyse.com) successfully.

Installation
------------

To use it in your sbt based project:

```
libraryDependencies ++= Seq(
  "com.github.mssd" %% "mssd" % "0.1.11"
)

resolvers += "fab mvn releases" at "https://github.com/fkoehler/fkoehler-mvn-repo/raw/master/releases/"
```

or if you are more into build.sbt style sbt syntax:
```
libraryDependencies += "com.github.mssd" %% "mssd" % "0.1.11"

resolvers += "fab mvn releases" at "https://github.com/fkoehler/fkoehler-mvn-repo/raw/master/releases/"
```

How to use it
-------------

