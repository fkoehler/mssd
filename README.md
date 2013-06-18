mad
===

MongoDB Asynchronous Driver for Scala based on mongodb-async-driver for Java from Roger J. Moore. See http://www.allanbank.com/mongodb-async-driver/index.html for more info about the driver.
This library provides a small layer over the java driver to provide a nice Scala-friendly API.

To use it in your sbt based project:

```
libraryDependencies ++= Seq(
  "com.github.mad" %% "mad" % "0.1-SNAPSHOT"
)

resolvers += "fab mvn snapshots" at "https://github.com/fkoehler/fkoehler-mvn-repo/raw/master/snapshots/"
```

The current snapshot is at:

It is already used in prouction and should be pretty stable as there is not a lot of code implemented.

Some intro and examples will follow ...

Feel free to contribute!
