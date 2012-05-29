package com.belfrygames.starkengine.script

import scala.util.parsing.combinator.JavaTokenParsers

object JSON extends JSON {
  val isObject: PartialFunction[Any, JSONObject] = { case n: JSONObject => n }
  val isList: PartialFunction[Any, JSONList[_]] = { case n: JSONList[_] => n }
  val isObjectList: PartialFunction[Any, JSONList[JSONObject]] = { case n: JSONList[_] if n.list.forall(_.isInstanceOf[JSONObject]) => n.asInstanceOf[JSONList[JSONObject]] }
  val isNumber: PartialFunction[Any, Double] = { case n: JSONNumber => n.value }
  val isString: PartialFunction[Any, String] = { case n: JSONLiteral => n.value }
  
  def parseText(text: String) = {
    parseAll(value, text)
  }
}

class JSON extends JavaTokenParsers {
  def obj: Parser[Map[String, Any]] =
    "{"~> repsep(member, ",") <~"}" ^^ (Map() ++ _)
  def arr: Parser[List[Any]] =
    "["~> repsep(value, ",") <~"]"
  def member: Parser[(String, Any)] =
    stringLiteral~":"~value ^^
  { case name~":"~value => (name.stripPrefix("\"").stripSuffix("\""), value) }
  
  def value: Parser[Any] = (
    obj
    | arr
    | stringLiteral ^^ (_.stripPrefix("\"").stripSuffix("\""))
    | floatingPointNumber ^^ (_.toDouble)
    | "null" ^^ (x => null)
    | "true" ^^ (x => true)
    | "false" ^^ (x => false)
  )
}

object JSONElement {
  def parse(m: Map[String, Any], container: Option[JSONObject] = None): JSONObject = {
    new JSONObject(m.map(e => (e._1, JSONElement.parseAll(e._2, container))))
  }
  
  def parse(l: List[Any], container: Option[JSONObject]): JSONList[JSONElement] = {
    new JSONList(l.map(parseAll(_, container)))
  }
  
  def parse(s: String, container: Option[JSONObject]): JSONLiteral = {
    new JSONLiteral(s, container)
  }
  
  def parse(d: Double, container: Option[JSONObject]): JSONNumber = {
    new JSONNumber(d, container)
  }
  
  def parseAll(elem: Any, container: Option[JSONObject]): JSONElement = {
    elem match {
      case m: Map[String, Any] => parse(m, container)
      case l: List[Any] => parse(l, container)
      case s: String => parse(s, container)
      case d: Double => parse(d, container)
      case x => sys.error("Unkown element defined in JSON object: " + toString() + " element:" + x)
    }
  }
}

class JSONElement(val container: Option[JSONObject] = None) {
  var parent: Option[JSONObject] = None
}

class JSONLiteral(val value: String, cont: Option[JSONObject] = None) extends JSONElement(cont)

class JSONNumber(val value: Double, cont: Option[JSONObject] = None) extends JSONElement(cont)

class JSONList[T <: JSONElement](val list: List[T], cont: Option[JSONObject] = None) extends JSONElement(cont) {
  //for(y <- 0 until list.list.size; x <- 0 until list.list.head.asInstanceOf[JSONList].list.size) {
  
  def size() = list.size
  
  /** Returns the element at the specified index */
  def apply(x: Int): T = {
    require(0 <= x && x < size)
    list(x)
  }
  
  /** Specialization method for when T is a JSONNumber simplifying access to data */
  def number(x: Int)(implicit evidence: T =:= JSONNumber): Double = {
    require(0 <= x && x < size)
    list(x).value
  }
  
  /** Specialization method for when T is a JSONList[JSONNumber] simplifying access to data */
  def apply(x: Int, y: Int)(implicit evidence: T =:= JSONList[JSONNumber]): Double = {
    require(0 <= x && x < size)
    list(x).number(y)
  }
  
}

class JSONObject(map0: Map[String, JSONElement], cont: Option[JSONObject] = None) extends JSONElement(cont) {
  /** Split templates from regular entries while removing '@' an extension declarations */
  val (templates, map) = {
    val (ts, es) = map0.partition(_._1.startsWith("@"))

    // Assign parents to elements that extend a template
    es.foreach(e => {
        e._2.parent = e._1.split("@").toList match {
          case List(name, template) => getTemplate(template)
          case List(name) => None
          case wrong => sys.error("Invalid key defined in JSON object: " + toString() + " key:" + wrong)
        }
      })

    def getTemplate(template: String): Option[JSONObject] = {
      ts.get("@" + template).orElse(container.flatMap(_.getTemplate(template))).map(_.asInstanceOf[JSONObject])
    }

    (ts.map(e => (e._1.substring(1), e._2)), es.map(e => (e._1.split("@").head, e._2)))
  }

  def getTemplate(template: String): Option[JSONObject] = {
    templates.get(template).orElse(container.flatMap(_.getTemplate(template))).map(_.asInstanceOf[JSONObject])
  }

  def apply(key: String): JSONElement = {
    if (parent.isDefined) {
      map.getOrElse(key, parent.get.apply(key))
    } else {
      map(key)
    }
  }

  def apply(key: String, default: JSONElement): JSONElement = {
    if (parent.isDefined) {
      map.getOrElse(key, parent.get.apply(key, default))
    } else {
      map.getOrElse(key, default)
    }
  }

  def find(f: String => Boolean) = {
    map.find(entry => f(entry._1))
  }

  def get(key: String): Option[JSONElement] = {
    map.get(key).orElse(parent.flatMap(_.get(key)))
  }

  def extractNumber(key: String, default: Int): Int = get(key) match {
    case Some(n: JSONNumber) => n.value.toInt
    case _ => default
  }
}
