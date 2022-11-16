package io.github.mostafamohajeri
package normativeservices

import bb.expstyla.exp.{BooleanTerm, DoubleTerm, GenericTerm, IntTerm, StringTerm, StructTerm, VarTerm}
import util.Value

import scala.jdk.CollectionConverters.CollectionHasAsScala

object ASCToEFlintStringifier {
  def createString(term: GenericTerm): String = term match {
    case v: VarTerm => createString(v.ref)
    case IntTerm(value) => value.toString
    case DoubleTerm(value) => value.toString
    case StringTerm(value) => s"\"$value\""
    case BooleanTerm(value) => value.toString
    case StructTerm(functor, terms) => functor match {
      case "," => terms.map(t=> createString(t)).mkString("&&")
      case _functor :String =>
        val newFunctor = _functor match {
          case "holds" => "?"
          case "enabled" => "?Enabled"
          case _ => _functor
        }
        newFunctor + (if (terms.nonEmpty) ("(" + terms.map(t => createString(t)).mkString(",") + ")")
        else "")
    }
  }

  def parseAsString(input: String) : GenericTerm = StringTerm(input)

  def parseTerm(input:String) : GenericTerm ={
    val structWithTerm= raw"([a-zA-Z_][a-zA-Z_0-9]*)\((.*)\)".r
    val structNoTermLowerStart= raw"([a-z_][a-zA-Z_0-9]*)".r
    val structNoTermUpperStart= raw"([A-Z_][a-zA-Z_0-9]*)".r
    val integer= raw"([0-9]+)".r
    val double= raw"([0-9]+\.[0.9]+)".r
    val string= raw""""([a-zA-Z\-_0-9]*)"""".r

    input match {
      case structWithTerm(f,terms) =>
        StructTerm(f,
          terms.split(",")
          .map(s=>s.trim)
          .map(parseTerm)
      )
      case structNoTermUpperStart(f) => StringTerm(f)
      case structNoTermLowerStart(f) => StructTerm(f)
      case integer(v) => IntTerm(v.toInt)
      case double(v) => DoubleTerm(v.toDouble)
      case string(v) => StringTerm(v)
      case "True" => BooleanTerm(true)
      case "False" => BooleanTerm(false)
      case _ => throw new RuntimeException(f"could not match $input")
     }
  }

  /**
   * parse Eflint's value to ASC2 terms
   * @note the eflint implementation is a bit off, there are different types encapsulated in one type, which is also propagated to this method
   * @param value
   * @return
   */
  def parseValue(value : Value) : GenericTerm = {
    if (value.int_value != null)
      return StructTerm(value.fact_type,Seq(IntTerm(value.int_value)))
    else if (value.string_value != null)
      return StructTerm(value.fact_type,Seq(StringTerm(value.string_value)))
    return StructTerm(value.fact_type,value.arguments.asScala.map(parseValue).toSeq)
  }

}
