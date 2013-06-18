package com.github.mssd

import scala.language.postfixOps

object Common {

  val client = MongoClient("localhost")
  val db = client("mssdtest")
  val coll = db("test")

}
