package net.virtualvoid.rescue
package impl

import scala.reflect.macros.Context

object Render {
  def rootCausesTree(withAnsi: Boolean)(search: ImplicitSearch): String = {
    val cols = colors(withAnsi)
    import cols._
    val tree = Analysis.reconstructSearchTree(search)

    val extraInfos = search.problems.filter(_.missingImplicit.isEmpty)

    def asterisks(idx: Int): String = (idx + 1) + ")"
    def renderInfo(tree: Tree): String = {
      import tree.info
      import TextTools.stripPrefixes
      val target = stripPrefixes(info.missingImplicit.getOrElse(info.forWhichType))
      val what = Option(info.whatImplicit).filter(_.nonEmpty).map(stripPrefixes).map(" @ "+).getOrElse("")
      val extra = if (info.missingImplicit.isEmpty) {
        val idx = extraInfos.indexOf(info)
        if (idx >= 0) " " + asterisks(idx) else ""
      } else ""

      s" $RED$target$RESET$what$extra"
    }
    val treeString = TreeRenderer.toAscii[Tree](tree, (_.children), renderInfo, 120)
    val extraInfoString =
      extraInfos.zipWithIndex.map {
        case (info, idx) ⇒
          asterisks(idx) + " " + info.message.replace("\n", " ")
      }.mkString("\n")
    s"""
         #Tree of failed implicit searches  (fixing leaf elements often fixes the parent):
         #
         #$treeString
         #$extraInfoString
         #
         #""".stripMargin('#')

  }
  def allProblemsString(search: ImplicitSearch): String = {
    def problemsMessage = search.problems.map { p ⇒
      f"${p.forWhichType}%-80s ${p.whatImplicit}%-50s ${p.message}"
    }.mkString("\n")

    s"""
       |Problems found during implicit search:
       |--------------------------------------
       |
       |$problemsMessage
       |""".stripMargin
  }
  def distinctBy[T, U](ts: Seq[T])(f: T ⇒ U): Seq[T] = ts.groupBy(f).map(_._2.head).toSeq

  trait Colors {
    def RED: String
    def GREEN: String
    def RESET: String

    def replaceColors(string: String): String =
      string.replace("[RED]", RED)
        .replace("[GRN]", GREEN)
        .replace("[RST]", RESET)
  }
  object AnsiColors extends Colors {
    def RED: String = Console.RED
    def GREEN: String = Console.GREEN
    def RESET: String = Console.RESET
  }
  object NoColors extends Colors {
    def RED: String = ""
    def GREEN: String = ""
    def RESET: String = ""
  }
  def colors(withAnsi: Boolean): Colors = if (withAnsi) AnsiColors else NoColors
}
