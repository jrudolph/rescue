package net.virtualvoid.rescue.impl

object TextTools {
  val DottedFormat = """((?:[^\[\]\.,_\s]+\.)*)(?!type)([^\[\]\.,_\s]+(?:\.type)?)""".r

  sealed trait TextElement {
    def isEmpty: Boolean
  }
  case class Literal(text: String) extends TextElement {
    def isEmpty: Boolean = text.isEmpty
  }
  case class Identifier(prefix: String, name: String) extends TextElement {
    def full: String = prefix + name
    def isEmpty = false
  }

  def findIdentifiers(identifier: String): Seq[TextElement] = {
    var cur = 0
    val res =
      TextTools.DottedFormat.findAllIn(identifier).matchData.flatMap { m ⇒ // dirty side-effecting map
        val prefix =
          if (m.start > cur) Seq(Literal(identifier.substring(cur, m.start)))
          else Nil

        cur = m.end

        prefix :+ Identifier(m.group(1), m.group(2))
      } ++ Seq(Literal(identifier.substring(cur, identifier.length)))

    res.toSeq.filterNot(_.isEmpty)
  }

  def stripPrefixes(text: String): String =
    findIdentifiers(text).map {
      case Literal(text)       ⇒ text
      case Identifier(_, name) ⇒ name
    }.mkString
}
