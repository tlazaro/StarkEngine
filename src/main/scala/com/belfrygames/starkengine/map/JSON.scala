package com.belfrygames.starkengine.map

import scala.util.parsing.combinator.JavaTokenParsers

object JSON extends JSON {
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

