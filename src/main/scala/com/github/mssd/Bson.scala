package com.github.mssd

import scala.language.implicitConversions
import org.joda.time.DateTime
import scala.util.matching.Regex


case object Bson {
  def doc(elements: (String, BsonElement)*): BsonDoc = new BsonDoc(elements)

  def arr(elements: BsonElement*): BsonArray = new BsonArray(elements)
}

trait ToBsonElement[-T] {
  def toBson(e: T): BsonElement
}

trait FromBsonElement[T] {
  def fromBson(d: BsonElement): T
}

trait ToBsonDoc[T] {
  def toBson(m: T): BsonDoc
}

trait FromBsonDoc[T] {
  def fromBson(d: BsonDoc): T
}

trait BsonImplicits extends ToBsonImplicits with FromBsonImplicits

trait ToBsonImplicits {

  implicit def seqToBsonElement[T](implicit c: ToBsonElement[T]) = new ToBsonElement[Seq[T]] {
    def toBson(v: Seq[T]): BsonElement = new BsonArray(v.map(c.toBson(_)))
  }

  implicit def mapToBsonElement[T](implicit c: ToBsonElement[T]) = new ToBsonElement[Map[String, T]] {
    def toBson(v: Map[String, T]): BsonElement = v.foldLeft(Bson.doc())((doc, kv) => doc + (kv._1 -> kv._2))
  }

  implicit def mapWithAnyToBsonElement = new ToBsonElement[Map[String, Any]] {
    def toBson(v: Map[String, Any]): BsonElement = v.foldLeft(Bson.doc())((doc, kv) => doc + (kv._1 -> new BsonAny(kv._2)))
  }

  implicit def optionToBsonElement[T](implicit c: ToBsonElement[T]) = new ToBsonElement[Option[T]] {
    def toBson(v: Option[T]): BsonElement = v match {
      case Some(v) => v
      case None => BsonNull
    }
  }

  implicit def anyType2BsonElement[T](element: T)(implicit c: ToBsonElement[T]): BsonElement = c.toBson(element)

  implicit object StringToBsonElement extends ToBsonElement[String] {
    def toBson(v: String): BsonElement = new BsonString(v)
  }

  implicit object IntToBsonElement extends ToBsonElement[Int] {
    def toBson(v: Int): BsonElement = new BsonInt(v)
  }

  implicit object LongToBsonElement extends ToBsonElement[Long] {
    def toBson(v: Long): BsonElement = new BsonLong(v)
  }

  implicit object DoubleToBsonElement extends ToBsonElement[Double] {
    def toBson(v: Double): BsonElement = new BsonDouble(v)
  }

  implicit object JodaDateTimeToBsonElement extends ToBsonElement[DateTime] {
    def toBson(v: DateTime): BsonElement = new BsonDateTime(v)
  }

  implicit object BooleanToBsonElement extends ToBsonElement[Boolean] {
    def toBson(v: Boolean): BsonElement = new BsonBoolean(v)
  }

  implicit object RegexToBsonElement extends ToBsonElement[Regex] {
    def toBson(v: Regex): BsonElement = new BsonRegex(v)
  }

}

trait FromBsonImplicits {

  implicit def seqFromBsonElement[T](implicit c: FromBsonElement[T]) = new FromBsonElement[Seq[T]] {
    def fromBson(v: BsonElement): Seq[T] = v match {
      case e: BsonDoc => Seq(c.fromBson(e))
      case e: BsonArray => e.elements.map(c.fromBson(_)).toList
    }
  }

  implicit def listFromBsonElement[T](implicit c: FromBsonElement[T]) = new FromBsonElement[List[T]] {
    def fromBson(v: BsonElement): List[T] = v match {
      case e: BsonArray => e.elements.map(c.fromBson(_)).toList
      case e: BsonElement => List(c.fromBson(e))
    }
  }

  implicit def mapFromBsonElement[T](implicit c: FromBsonElement[T]) = new FromBsonElement[Map[String, T]] {
    def fromBson(v: BsonElement): Map[String, T] = v.asInstanceOf[BsonDoc].elements.foldLeft(Map[String, T]())((map, kv) => map + (kv._1 -> c.fromBson(kv._2)))
  }

  implicit def mapWithAnyFromBsonElement = new FromBsonElement[Map[String, Any]] {
    def fromBson(v: BsonElement): Map[String, Any] = v.asInstanceOf[BsonDoc].elements.foldLeft(Map[String, Any]())((map, kv) => map + (kv._1 -> (kv._2 match {
      case BsonString(v) => v
      case BsonInt(v) => v
      case BsonLong(v) => v
      case BsonDouble(v) => v
    })))
  }

  implicit def optionFromBsonElement[T](implicit c: FromBsonElement[T]) = new FromBsonElement[Option[T]] {
    def fromBson(v: BsonElement): Option[T] = v match {
      case BsonNull => None
      case e: BsonElement => Some(c.fromBson(e))
    }
  }

  implicit def anyTypeFromBsonElement[T](element: BsonElement)(implicit c: FromBsonElement[T]): T = c.fromBson(element)

  implicit object StringFromBsonElement extends FromBsonElement[String] {
    def fromBson(v: BsonElement): String = v.asInstanceOf[BsonString].value
  }

  implicit object DocFromBsonElement extends FromBsonElement[BsonDoc] {
    def fromBson(v: BsonElement): BsonDoc = v.asInstanceOf[BsonDoc]
  }

  implicit object IntFromBsonElement extends FromBsonElement[Int] {
    def fromBson(v: BsonElement): Int = v.asInstanceOf[BsonInt].value
  }

  implicit object LongFromBsonElement extends FromBsonElement[Long] {
    def fromBson(v: BsonElement): Long = v.asInstanceOf[BsonLong].value
  }

  implicit object DoubleFromBsonElement extends FromBsonElement[Double] {
    def fromBson(v: BsonElement): Double = v.asInstanceOf[BsonDouble].value
  }

  implicit object JodaDateTimeFromBsonElement extends FromBsonElement[DateTime] {
    def fromBson(v: BsonElement): DateTime = v.asInstanceOf[BsonDateTime].value
  }

  implicit object BooleanFromBsonElement extends FromBsonElement[Boolean] {
    def fromBson(v: BsonElement): Boolean = v.asInstanceOf[BsonBoolean].value
  }

  implicit object RegexFromBsonElement extends FromBsonElement[Regex] {
    def fromBson(v: BsonElement): Regex = v.asInstanceOf[BsonRegex].value
  }

}

case class BsonDoc(elements: Seq[(String, BsonElement)]) extends BsonElement {
  /** merge with other doc */
  def ++(doc: BsonDoc): BsonDoc = BsonDoc(elements ++ doc.elements)

  /** add key, value pair */
  def +(keyValue: (String, BsonElement)): BsonDoc = BsonDoc(elements :+ keyValue)

  def keys: Seq[String] = elements.map(_._1)

  def get(key: String): BsonElement = elements.find(kv => kv._1 == key).get._2

  def getAs[T](key: String): T = get(key).asInstanceOf[T]

  def apply[T](key: String)(implicit c: FromBsonElement[T]): T = as(key)

  def as[T](key: String)(implicit c: FromBsonElement[T]): T = try {
    c.fromBson(get(key))
  } catch {
    case e: java.lang.ClassCastException => println("Class cast exception during load for key " + key + " of doc : " + toString()); throw e;
  }

  def asOpt[T](key: String)(implicit c: FromBsonElement[T]): Option[T] = try {
    elements.find(kv => kv._1 == key) match {
      case Some(kv) => Some(c.fromBson(kv._2))
      case None => None
    }
  } catch {
    case e: java.lang.ClassCastException => println("Class cast exception during load for key " + key + " of doc : " + toString()); throw e;
  }

  /** return None if the key is found but the value is a BsonNull **/
  def asNullableOpt[T](key: String)(implicit c: FromBsonElement[T]): Option[T] = try {
    elements.find(kv => kv._1 == key) match {
      case Some(kv) => kv._2 match {
        case null => None
        case BsonNull => None
        case e: BsonElement => Some(c.fromBson(e))
      }
      case None => None
    }
  } catch {
    case e: java.lang.ClassCastException => println("Class cast exception during load for key " + key + " of doc : " + toString()); throw e;
  }

  override def toString() = toStr()

  import BsonDoc.indent

  def toStr(level: Int = 0): String = "{\n" + elements.foldLeft("") {
    (s, kv) =>
      s + indent(level) + s"${kv._1}: " + (if (kv._2.isInstanceOf[BsonDoc]) {
        kv._2.asInstanceOf[BsonDoc].toStr(level + 1)
      } else {
        kv._2.toStr(level) + ",\n"
      })
  } + indent(level - 1) + "}\n"

}

case class BsonArray(elements: Seq[BsonElement]) extends BsonElement {
  def asDocs: Seq[BsonDoc] = elements.map(_.asInstanceOf[BsonDoc])

  /** append to array */
  def :+(element: BsonElement): BsonArray = BsonArray(elements :+ element)

  import BsonDoc.indent

  def toStr(level: Int) = "[" + elements.map("\n" + indent(level + 1) + _.toStr(level + 2)).mkString(",") + "\n" + indent(level) + "]"
}

case class BsonInt(value: Int) extends BsonElement {
  def toStr(level: Int) = value.toString
}

case class BsonObjectId(value: String) extends BsonElement {
  def toStr(level: Int) = value.toString
}

case class BsonLong(value: Long) extends BsonElement {
  def toStr(level: Int) = value.toString
}

case class BsonString(value: String) extends BsonElement {
  def toStr(level: Int) = '"' + value.toString + '"'
}

case class BsonDouble(value: Double) extends BsonElement {
  def toStr(level: Int) = value.toString
}

case class BsonDateTime(value: DateTime) extends BsonElement {
  def toStr(level: Int) = value.toString
}

case class BsonBoolean(value: Boolean) extends BsonElement {
  def toStr(level: Int) = value.toString
}

case class BsonRegex(value: Regex) extends BsonElement {
  def toStr(level: Int) = "/" + value.toString + "/"
}

case class BsonAny(value: Any) extends BsonElement {
  def toStr(level: Int) = value.toString
}

case object BsonNull extends BsonElement {
  def toStr(level: Int) = "null"
}

case object BsonDoc {
  def indent(level: Int) = s"${"  " * (level + 1)}"
}

abstract class BsonElement {
  def getAs[T]: T = this.asInstanceOf[T]

  def as[T](implicit c: FromBsonElement[T]): T = c.fromBson(this)

  def toStr(level: Int): String
}
