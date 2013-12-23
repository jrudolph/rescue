package net.virtualvoid.rescue.impl

/* sbt -- Simple Build Tool
 * Copyright 2011 Mark Harrah, Eugene Yokota
 *
 * Copied from sbt 0.12 source code
 */
object TreeRenderer {
  // [info] foo
  // [info]   +-bar
  // [info]   | +-baz
  // [info]   |
  // [info]   +-quux
  def toAscii[A](top: A,
                 children: A ⇒ Seq[A],
                 display: A ⇒ String,
                 maxColumn: Int = defaultColumnSize): String = {
    val twoSpaces = " " + " " // prevent accidentally being converted into a tab
    def limitLine(s: String): String =
      if (s.length > maxColumn) s.slice(0, maxColumn - 2) + ".."
      else s
    def insertBar(s: String, at: Int): String =
      if (at < s.length)
        s.slice(0, at) +
          (s(at).toString match {
            case " " ⇒ "|"
            case x   ⇒ x
          }) +
          s.slice(at + 1, s.length)
      else s
    def toAsciiLines(node: A, level: Int): Vector[String] = {
      val line = limitLine((twoSpaces * level) + (if (level == 0) "" else "+-") + display(node))
      val cs = Vector(children(node): _*)
      val childLines = cs map { toAsciiLines(_, level + 1) }
      val withBar = childLines.zipWithIndex flatMap {
        case (lines, pos) if pos < (cs.size - 1) ⇒ lines map { insertBar(_, 2 * (level + 1)) }
        case (lines, pos) ⇒
          if (lines.last.trim != "") lines ++ Vector(twoSpaces * (level + 1))
          else lines
      }
      line +: withBar
    }

    toAsciiLines(top, 0).mkString("\n")
  }

  def defaultColumnSize: Int = 80
}
