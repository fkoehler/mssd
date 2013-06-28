package com.github.mssd

import com.mongodb.{MongoClient => LegacyMongoClient, _}
import com.mongodb.gridfs.GridFS
import com.github.mssd.Implicits._

object MongoClient {
  val defaultDurability = WriteConcern.ACKNOWLEDGED

  def apply(host: String, options: MongoClientOptions): MongoClient = {
    new MongoClient(new LegacyMongoClient(new ServerAddress(host), options))
  }

  def apply(host: String, port: Int, options: MongoClientOptions): MongoClient = {
    new MongoClient(new LegacyMongoClient(new ServerAddress(host, port), options))
  }

  def apply(host: String): MongoClient = {
    new MongoClient(new LegacyMongoClient(host))
  }
}

class MongoClient(val underlying: LegacyMongoClient) {
  def apply(databaseName: String): MongoDatabase = new MongoDatabase(underlying.getDB(databaseName))

  def readPreference: ReadPreference = underlying.getReadPreference

  def readPreference_=(pref: ReadPreference) = underlying.setReadPreference(pref)
}

class MongoDatabase(val underlying: DB) {

  def name = underlying.getName

  def apply(collectionName: String): MongoCollection = new MongoCollection(underlying.getCollection(collectionName))

  def durability_=(durability: WriteConcern) = underlying.setWriteConcern(durability)

  def durability = underlying.getWriteConcern

  def gridFs: GridFS = new GridFS(underlying)

  def gridFs(rootName: String): GridFS = new GridFS(underlying, rootName)

  def createCollection(name: String, options: BsonDoc) = underlying.createCollection(name, options)

  def createCappedCollection(name: String, sizeInBytes: Long, maxNrOfDocs: Option[Int]) = underlying.createCollection(name, Bson.doc(
    "capped" -> true,
    "size" -> sizeInBytes
  ) ++ (maxNrOfDocs match {
    case Some(size) => Bson.doc("max" -> size)
    case None => Bson.doc()
  }))

}