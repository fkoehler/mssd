package com.github.mssd

import org.joda.time.{Instant, DateTime}
import com.mongodb.{BasicDBList, BasicDBObjectBuilder, DBObject}
import org.bson.types.ObjectId
import java.lang
import scala.Some

object Implicits extends BsonDocImplicits with BsonImplicits

trait BsonDocImplicits {

  import scala.language.implicitConversions
  import scala.collection.JavaConversions._

  implicit def bsonDoc2DBObject(bsonDoc: BsonDoc): DBObject = {
    if (bsonDoc == null) {
      return null
    }

    val builder = BasicDBObjectBuilder.start

    for ((key, element) <- bsonDoc.elements) {
      element match {
        case BsonArray(v) => {
          Double
          val list = new BasicDBList()
          v.foreach(elem => list.add(bsonElement2Element(elem)))
          builder.add(key, list)
        }
        case _ => builder.add(key, bsonElement2Element(element))
      }
    }

    builder.get
  }

  implicit def bsonElement2Element(element: BsonElement): AnyRef = {
    element match {
      case BsonObjectId(v) => new ObjectId(v)
      case BsonDouble(v) => new lang.Double(v)
      case BsonString(v) =>
        Option(v) match {
          case Some(v) => v
          case None => null
        }
      case BsonBoolean(v) => new java.lang.Boolean(v)
      case BsonRegex(v) => v.pattern
      case BsonInt(v) => new java.lang.Integer(v)
      case BsonLong(v) => new java.lang.Long(v)
      case BsonDateTime(v) => v.toInstant.toDate
      case doc@BsonDoc(v) => bsonDoc2DBObject(doc)
      case null => null
      case BsonNull => null
      case BsonAny(v) => v match {
        case v: String => v
        case v: Int => new java.lang.Integer(v)
        case v: Double => new lang.Double(v)
        case v: Long => new java.lang.Long(v)
        case v: DateTime => v.toInstant.toDate
        case v: Boolean => new java.lang.Boolean(v)
      }
      case v@_ => throw new RuntimeException("not yet implemented for value: " + v)
    }
  }

  implicit def dbObject2BsonDoc(doc: DBObject): BsonDoc = {
    if (doc == null)
      return null

    var bsonDoc = Bson.doc()

    for ((key, value) <- doc.toMap.asInstanceOf[java.util.HashMap[String, Object]]) {
      bsonDoc += key -> elementToBsonElement(value)
    }

    bsonDoc
  }

  def elementToBsonElement(element: Any): BsonElement = {
    element match {
      case o: ObjectId => BsonObjectId(o.toString)
      case e: java.lang.Integer => BsonInt(e)
      case e: java.lang.Double => BsonDouble(e)
      case e: java.lang.Long => BsonLong(e)
      case e: String => BsonString(e)
      case null => BsonNull
      case e: java.lang.Boolean => BsonBoolean(e)
      case e: java.util.regex.Pattern => BsonRegex(e.toString.r)
      case e: java.util.Date => BsonDateTime(new Instant(e.getTime).toDateTime)
      case e: BasicDBList => e.foldLeft(Bson.arr()) {
        (array, element) =>
          array :+ elementToBsonElement(element)
      }
      case e: DBObject => dbObject2BsonDoc(e)
      case _ => throw new RuntimeException("not yet implemented. go ahead: " + element)
    }
  }

}
