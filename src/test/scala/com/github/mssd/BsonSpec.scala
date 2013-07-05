package com.github.mssd

import org.specs2.mutable.Specification
import org.joda.time.DateTime
import com.mongodb.{BasicDBList, BasicDBObjectBuilder}
import java.util.regex.Pattern

class BsonSpec extends Specification {

  import Implicits._

  "The more scala like bson builder classes" should {
    "convert a doc with multiple types into a proper document" in {
      val op: Option[Int] = None
      val date = DateTime.now
      val bsonDoc = Bson.doc(
        "_id" -> 1,
        "name" -> "fabian",
        "double" -> 34.45,
        "bool" -> true,
        "date" -> date,
        "optionSome" -> Some(5),
        "optionNone" -> op,
        "regex" -> "a?b".r,
        "subDoc" -> Bson.doc("subId" -> 2),
        "stringarray" -> Bson.arr(
          "string",
          "string2"
        ),
        "stringarrayWithList" -> List(
          "string",
          "string2"
        ),
        "docarray" -> Bson.arr(
          Bson.doc("_id2" -> 45),
          Bson.doc("_id3" -> 77)
        )
      )

      val docArray = new BasicDBList()
      docArray.add(BasicDBObjectBuilder.start.add("_id2", 45).get())
      docArray.add(BasicDBObjectBuilder.start.add("_id3", 77).get())

      val stringList = new BasicDBList()
      stringList.add("string")
      stringList.add("string2")
      val doc = BasicDBObjectBuilder.start
        .add("_id", 1)
        .add("name", "fabian")
        .add("double", 34.45)
        .add("bool", true)
        .add("date", date.toDate())
        .add("optionSome", 5)
        .add("optionNone", null)
        .add("regex", Pattern.compile("a?b"))
        .add("subDoc", BasicDBObjectBuilder.start().add("subId", 2).get)
        .add("stringarray", stringList)
        .add("stringarrayWithList", stringList)
        .add("docarray", docArray)
        .get

      bsonDoc2DBObject(bsonDoc).toString must equalTo(doc.toString)
      dbObject2BsonDoc(doc).toString must equalTo(bsonDoc.toString)
    }
  }

}