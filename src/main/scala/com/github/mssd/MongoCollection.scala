package com.github.mssd

import Implicits._
import com.mongodb._
import scala.Some
import scala.collection.JavaConversions._

class MongoCollection(val underlying: DBCollection) extends SyncCollection {

  def name = underlying.getName

  def drop() = underlying.drop()

  def durability_=(durability: WriteConcern) = underlying.setWriteConcern(durability)

  def durability = underlying.getWriteConcern

  def createIndex(doc: BsonDoc, options: BsonDoc = Bson.doc()) = underlying.createIndex(doc, options)

  def rename(newName: String, dropTarget: Boolean = false): MongoCollection = new MongoCollection(underlying.rename(newName, dropTarget))

}

trait SyncCollection {
  val underlying: DBCollection

  def delete() = underlying.remove(Bson.doc())

  def remove(query: BsonDoc) = underlying.remove(query)

  def insert[T](model: T)(implicit c: ToBsonDoc[T]): WriteResult = underlying.insert(c.toBson(model))

  def insert(doc: BsonDoc): WriteResult = underlying.insert(doc)

  def save(doc: BsonDoc): WriteResult = underlying.save(doc)

  def save[T](model: T)(implicit c: ToBsonDoc[T]): WriteResult = underlying.save(c.toBson(model))

  def update(query: BsonDoc, update: BsonDoc, multi: Boolean = false, upsert: Boolean = false): WriteResult = underlying.update(query, update, upsert, multi)

  /** really find one doc by any query, not just id like in legacy **/
  def findOne(doc: BsonDoc): Option[BsonDoc] = findOne(Find().query(doc))

  def findOne(findv: Find): Option[BsonDoc] = {
    val cursor = find(findv)
    if (cursor.hasNext)
      Option(cursor.next())
    else
      None
  }

  def findOneAs[T](doc: BsonDoc)(implicit c: FromBsonDoc[T]): Option[T] = findOneAs(Find().query(doc))

  def findOneAs[T](find: Find)(implicit c: FromBsonDoc[T]): Option[T] = findOne(find).map(c.fromBson)

  def findOneById(id: Int): Option[BsonDoc] = findOne(Bson.doc("_id" -> id))

  def findOneById(id: String): Option[BsonDoc] = findOne(Bson.doc("_id" -> id))

  def findOneByIdAs[T](id: String)(implicit c: FromBsonDoc[T]): Option[T] = findOneById(id).map(c.fromBson)

  def find(query: BsonDoc): MongoIterator = MongoIterator(underlying.find(query), Find())

  def find(find: Find): MongoIterator = MongoIterator(underlying.find(find._query, find._keys), find)

  def findAs[T](find: Find)(implicit c: FromBsonDoc[T]): Iterator[T] = {
    val underlyingIter = underlying.find(find._query, find._keys)
    find.applyCursorSettings(underlyingIter)
    new Iterator[T] {
      def hasNext: Boolean = underlyingIter.hasNext

      def next(): T = c.fromBson(underlyingIter.next())
    }
  }

  def findAndApply(query: BsonDoc)(docFunc: (BsonDoc) => Unit): Unit = find(query).foreach(docFunc)

  def findAndApply(f: Find)(docFunc: (BsonDoc) => Unit): Unit = find(f).foreach(docFunc)

  def findAndApplyAs[T](query: BsonDoc)(docFunc: (T) => Unit)(implicit c: FromBsonDoc[T]): Unit =
    find(query).map(c.fromBson).foreach(docFunc)

  def findAndApplyAs[T](f: Find)(docFunc: (T) => Unit)(implicit c: FromBsonDoc[T]): Unit =
    find(f).map(c.fromBson).foreach(docFunc)

  def count: Long = underlying.count()

  def count(query: BsonDoc): Long = underlying.count(query)

  def distinct(field: String, query: BsonDoc): List[String] = underlying.distinct(field, query).asInstanceOf[BasicDBList]
    .map(elementToBsonElement(_).as[String]).toList

  def group(keys: BsonDoc, cond: BsonDoc, initial: BsonDoc, reduce: String): BsonArray =
    underlying.group(keys, cond, initial, reduce).asInstanceOf[BasicDBList].foldLeft(Bson.arr()) {
    (array, element) =>
      array :+ elementToBsonElement(element)
  }

}

object Find {
  def apply(): Find = new Find()
}

case class Find(_query: BsonDoc = Bson.doc(), _sort: Option[BsonDoc] = None, _keys: BsonDoc = null,
                _batchSize: Option[Int] = None, _limit: Option[Int] = None,
                _skip: Option[Int] = None) {

  def applyCursorSettings(cursor: DBCursor) {
    _batchSize.foreach(cursor.batchSize)
    _skip.foreach(cursor.skip)
    _limit.foreach(cursor.limit)
    _sort.foreach(cursor.sort(_))
  }

  def query(doc: BsonDoc): Find = copy(_query = doc)

  def keys(keys: BsonDoc) = copy(_keys = keys)

  def sort(sort: BsonDoc) = copy(_sort = Some(sort))

  def batchSize(bs: Int) = copy(_batchSize = Some(bs))

  def limit(n: Int) = copy(_limit = Some(n))

  def skip(n: Int) = copy(_skip = Some(n))

}

object MongoIterator {
  def apply(underlying: DBCursor, find: Find): MongoIterator = new MongoIterator(underlying, find)

}

class MongoIterator(val underlying: DBCursor, find: Find) extends Iterator[BsonDoc] {

  find.applyCursorSettings(underlying)

  def hasNext: Boolean = underlying.hasNext

  def next(): BsonDoc = underlying.next

  def close() = underlying.close()

}