# MongoDB Synchronous Scala Driver # 

MongoDB Synchronous Scala Driver (mssd) is based on the plain old legacy [mongodb java driver](http://docs.mongodb.org/ecosystem/drivers/java/) and adds some Scala beauty to it.
It's similar to [casbash](http://mongodb.github.io/casbah/) but with a different api which was inspired by the [Play2 JSON API](http://www.playframework.com/documentation/2.2.0/ScalaJson) for the bson documents and by the [mongodb-async-driver](http://www.allanbank.com/mongodb-async-driver/index.html) for it's general API.

This driver is work in progress and not complete yet. Please feel free to contribute. It is used in production at [WebPageAnalyse](http://www.webpageanalyse.com) successfully.

## Installation ##

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

In order to get a collection to work on you setup the driver like shown below. Of course you can specify any normal options. There might not be a wrapper for everything yet.

```
val client = MongoClient("hostname", 27017,
    new MongoClientOptions.Builder()
      .autoConnectRetry(true)
      .maxAutoConnectRetryTime(30000)
      .threadsAllowedToBlockForConnectionMultiplier(1000)
      .connectionsPerHost(1000)
      .build())

val db = client("dbname")
val collection = db("collectionname")
```

## Some simple use cases ##
### Save doc, get it back and delete it ###
```

```

### More complex find ###
```
```

## Using case classes for mapping ##

There is no magic involved here. You have to manually define implicit converters which convert either a BsonElement or a BsonDoc into whatever you want. This was a clear decision in the beginning that we wanted to have manually mappings as we tend to define short keys (to save space) in our docs or do other conversion stuff.

### Define a case class which we want to map ###
```
```

### Save and restore it from the db ###
```
```

Please have a look in the source code and tests for more details. It's not too hard to understand I guess as it basically wraps over the existing driver.
