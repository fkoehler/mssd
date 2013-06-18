package com.github.mssd

import concurrent.Await
import Implicits._
import scala.Predef._
import org.specs2.mutable.Specification
import org.specs2.specification.BeforeExample
import org.joda.time.DateTime

class CollectionSpec extends Specification with BeforeExample {
  sequential

  import Common._

  protected def before = coll.drop()

  val simpleDoc = Bson.doc("_id" -> 1, "name" -> "fabian")

  "The collection class" should {
    "load an objectid" in {
      coll.insert(Bson.doc("name" -> "fabian"))
      val doc = coll.findOne(Bson.doc("name" -> "fabian")).get
      doc.as[String]("name") must equalTo("fabian")
      doc.get("_id") must beAnInstanceOf[BsonObjectId]
    }
    "insert a simple doc correctly sync" in {
      coll.insert(simpleDoc)
      coll.findOneById(1).get must equalTo(simpleDoc)
    }
    "update a simple doc correctly sync" in {
      coll.insert(simpleDoc)
      coll.update(simpleDoc, Bson.doc("$set" -> Bson.doc("name" -> "köhler")))
      coll.findOneById(1).get[String]("name") must equalTo("köhler")
    }
    "upsert a simple doc correctly sync" in {
      coll.update(Bson.doc("_id" -> 1, "name" -> "köhler"), simpleDoc, upsert = true)
      coll.findOneById(1).get must equalTo(simpleDoc)
    }
    "save a simple doc correctly sync" in {
      coll.save(simpleDoc)
      coll.findOneById(1).get must equalTo(simpleDoc)
    }
    "find builder with skip and limit" in {
      coll.save(Bson.doc("_id" -> 60, "name" -> "fabian"))
      coll.save(Bson.doc("_id" -> 70, "name" -> "fabian"))
      coll.save(Bson.doc("_id" -> 80, "name" -> "fabian"))

      val find = Find()
        .query(Bson.doc("name" -> "fabian"))
        .limit(1)
        .skip(1)

      coll.find(find).size must beEqualTo(1)
      coll.find(find).next must beEqualTo(Bson.doc("_id" -> 70, "name" -> "fabian"))
    }
    "find a doc by query (managed resource)" in {
      coll.save(simpleDoc)
      coll.find(Bson.doc("_id" -> 1)).size must beEqualTo(1)
      coll.find(Bson.doc("_id" -> 1)).next must beEqualTo(simpleDoc)
    }
    "find a doc by query (provide function to execute for every doc)" in {
      coll.save(simpleDoc)
      coll.findAndApply(Bson.doc("_id" -> 1)) {
        doc =>
          doc must beEqualTo(simpleDoc)
      }
    }
    "insert a date time correctly full roundtrip" in {
      val date = DateTime.now
      coll.insert(Bson.doc("_id" -> 99, "datet" -> date))
      coll.findOneById(99).get[DateTime]("datet") must beEqualTo(date)
    }
  }

}