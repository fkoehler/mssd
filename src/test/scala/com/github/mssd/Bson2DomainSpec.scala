package com.github.mssd

import Implicits._
import scala.language.implicitConversions
import org.specs2.specification.BeforeExample
import org.specs2.mutable.Specification

case class Model(id: Int, value: Int, keys: Map[String, Any] = Map(), op: Option[Int] = None, list: List[String] = List(),
                 listObj: List[Model] = List())

case object Model {

  implicit object ModelToBsonElement extends ToBsonElement[Model] {
    def toBson(v: Model): BsonElement = ModelToBsonDoc.toBson(v)
  }

  implicit object ModelFromBsonElement extends FromBsonElement[Model] {
    def fromBson(v: BsonElement): Model = ModelFromBsonDoc.fromBson(v.asInstanceOf[BsonDoc])
  }

  implicit object ModelFromBsonDoc extends FromBsonDoc[Model] {
    def fromBson(d: BsonDoc): Model = Model(
      d[Int]("_id"),
      d[Int]("value"),
      d[Map[String, Any]]("keys"),
      d[Option[Int]]("op"),
      d[List[String]]("list"),
      d[List[Model]]("listObj")
    )
  }

  implicit object ModelToBsonDoc extends ToBsonDoc[Model] {
    def toBson(m: Model): BsonDoc = Bson.doc(
      "_id" -> m.id,
      "value" -> m.value,
      "keys" -> m.keys,
      "op" -> m.op,
      "list" -> m.list,
      "listObj" -> m.listObj
    )
  }

}

class Bson2DomainSpec extends Specification with BeforeExample {
  sequential

  import Common._

  protected def before = coll.drop

  "A bson doc" should {
    "should convert a sub doc properly when pulling it out" in {
      val doc = Bson.doc("test" -> Bson.doc("key" -> 1))
      doc.asOpt[BsonDoc]("test").get.as[Int]("key") must beEqualTo(1)
    }
    "should support an option for a BsonDoc which has Null values which should be None" in {
      val doc = Bson.doc(
        "test" -> null,
        "test2" -> "fabse"
      )
      doc.asNullableOpt[String]("test") must beEqualTo(None)
      doc.asNullableOpt[String]("test2").get must beEqualTo("fabse")
    }
    "should convert a doc to a List if it is a single item" in {
      val doc = Bson.doc(
        "test" -> Bson.doc("test2" -> "fabse")
      )
      doc.as[List[BsonDoc]]("test") must beEqualTo(List(Bson.doc("test2" -> "fabse")))
    }
    "should convert a string to a List because it is a single item" in {
      val doc = Bson.doc(
        "test" -> "fabse"
      )
      doc.as[List[String]]("test") must beEqualTo(List("fabse"))
    }
    "should convert a doc to a Seq if it is a single item" in {
      val doc = Bson.doc(
        "test" -> Bson.doc("test2" -> "fabse")
      )
      doc.as[Seq[BsonDoc]]("test") must beEqualTo(Seq(Bson.doc("test2" -> "fabse")))
    }
    "should convert a Bson.arr() to a List as usual" in {
      val doc = Bson.doc(
        "test" -> Bson.arr(Bson.doc("test2" -> "fabse"))
      )
      doc.as[List[BsonDoc]]("test") must beEqualTo(List(Bson.doc("test2" -> "fabse")))
    }
    "should convert case clases and scala data structures to a proper document" in {
      val bsonDoc = Bson.doc(
        "_id" -> 1,
        "listString" -> List("3", "4", "5"),
        "mapStringString" -> Map("key" -> "v1", "key2" -> "v2"),
        "mapStringDouble" -> Map("key" -> 45.5, "key2" -> 23.2),
        "mapStringAny" -> Map("key" -> 45.5, "key2" -> "2"),
        "object" -> Model(1, 5, Map("k1" -> "v1"), None, List("Fabian", "rockt"), List(Model(2, 6))),
        "listObjects" -> List(Model(1, 5), Model(2, 6))
      )

      val op: Option[Int] = None
      true must beEqualTo(true)
      //      bsonDoc must beEqualTo(Bson.doc(
      //        "_id" -> 1,
      //        "listString" -> Bson.arr("3", "4", "5"),
      //        "mapStringString" -> Bson.doc("key" -> BsonAny("v1"), "key2" -> BsonAny("v2")),
      //        "mapStringDouble" -> Bson.doc("key" -> BsonAny(45.5), "key2" -> BsonAny(23.2)),
      //        "mapStringAny" -> Bson.doc("key" -> BsonAny(45.5), "key2" -> BsonAny("2")),
      //        "object" -> Bson.doc(
      //          "_id" -> 1,
      //          "value" -> 5,
      //          "keys" -> Bson.doc("k1" -> "v1"),
      //          "op" -> op,
      //          "list" -> Bson.arr("Fabian", "rockt"),
      //          "listObj" -> Bson.arr(Bson.doc("_id" -> 2, "value" -> 6, "keys" -> Bson.doc(), "op" -> op, "list" -> Bson.arr(), "listObj" -> Bson.arr()))
      //        ),
      //        "listObjects" -> Bson.arr(
      //          Bson.doc("_id" -> 1, "value" -> 5, "keys" -> Bson.doc(), "op" -> op, "list" -> Bson.arr(), "listObj" -> Bson.arr()),
      //          Bson.doc("_id" -> 2, "value" -> 6, "keys" -> Bson.doc(), "op" -> op, "list" -> Bson.arr(), "listObj" -> Bson.arr())
      //        )
      //      ))
    }
  }
  "A bson doc 2" should {
    "should222 convert case clases and scala data structures to a proper document" in {
      val bsonDoc = Bson.doc(
        "_id" -> 1,
        "listString" -> List("3", "4", "5")
      )

      bsonDoc must beEqualTo(Bson.doc(
        "_id" -> 1,
        "listString" -> Bson.arr("3", "4", "5")
      ))
    }
  }
  "i want read and write nicely with case classes and it" should {
    "work" in {
      val model = Model(1, 5, Map("k1" -> "v1", "k2" -> 45), None, List("Fabian", "rockt"), List(Model(2, 6)))
      coll.insert(model)
      val result = coll.findOneAs[Model](Bson.doc("_id" -> 1)).get

      model must beEqualTo(result)
    }
  }

}