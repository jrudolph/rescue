package net.virtualvoid.rescue
package impl

import scala.language.experimental.macros

import scala.reflect.macros.{ TypecheckException, Context }
import scala.tools.nsc.reporters.Reporter
import scala.reflect.internal.util.Position
import scala.util.control.NonFatal

object ImplicitsMacros {
  sealed trait Problem
  sealed trait NotAValidImplicitValueForProblem extends Problem {
    def what: String
    def forT: String
    def message: String
  }
  case class HasMatchingSymbolError(what: String, forT: String, message: String) extends NotAValidImplicitValueForProblem
  case class OtherError(what: String, forT: String, message: String) extends NotAValidImplicitValueForProblem
  case class UnknownProblem(msg: String) extends Problem

  // we need to keep track for which positions we have already shown something
  // because the macro will be called several times
  var alreadyShown: Set[(AnyRef, String)] = Set.empty

  def rootCauses[T: c.WeakTypeTag](c: Context): c.Expr[T] =
    runWithPrinter(c)(Render.rootCausesTree(withAnsi = true))

  def rootCausesNoAnsi[T: c.WeakTypeTag](c: Context): c.Expr[T] =
    runWithPrinter(c)(Render.rootCausesTree(withAnsi = false))

  def runWithPrinter[T: c.WeakTypeTag](c: Context)(render: ImplicitSearch ⇒ String): c.Expr[T] = {
    val tpe = c.openImplicits.head._1

    val result = new Analyzer(c).run
    if (c.openImplicits.size <= 1 && result.nonEmpty) {
      val search = result.head
      val text =
        try render(search)
        catch {
          case NonFatal(e) ⇒ e.printStackTrace(); "failed"
        }
      if (!alreadyShown(c.enclosingPosition -> text)) {
        c.echo(c.universe.NoPosition, text)
        alreadyShown += c.enclosingPosition -> text
      }
    }
    c.abort(c.enclosingPosition, "")

    c.universe.reify {
      null.asInstanceOf[T]
    }
  }

  def problemRank(p: ProblemInfo) = (p.message, p.whatImplicit)

  val reporter = {
    val field = classOf[scala.tools.nsc.Global].getDeclaredField("reporter")
    field.setAccessible(true)
    field
  }
  val info0M = {
    val method = classOf[Reporter].getDeclaredMethods.find(_.getName == "info0").get
    method.setAccessible(true)
    method
  }
  val MessageFormat = s"""(?s:(.*?) is not a valid implicit value for (.*?) because:${"\n"}(.*))""".r
  val HasMatchingSymbolFormat = """(?s)hasMatchingSymbol reported error: (.*)""".r

  class Analyzer(val c: Context) {
    def run = {
      val tpe = c.openImplicits.head._1 //c.weakTypeTag[T]

      //println("Was called " + c.openImplicits.size + " " + c.openMacros.size + " for type " + tpe)
      //if (c.openImplicits.size > 1)
      //else
      //println(c.openImplicits, c.openMacros)

      val intC = c.asInstanceOf[scala.reflect.macros.runtime.Context]
      val settings = intC.global.settings
      def withMyReporter[R](r: Reporter ⇒ Reporter)(body: ⇒ R): R = {
        val originalReporter = intC.global.reporter
        try {
          reporter.set(intC.global, r(originalReporter))
          body
        } finally reporter.set(intC.global, originalReporter)
      }
      def withSetting[T, R](setting: settings.BooleanSetting, newValue: Boolean)(body: ⇒ R): R = {
        val oldValue = setting.value
        try {
          setting.value = newValue
          body
        } finally setting.value = oldValue
      }

      var problems: List[Problem] = Nil

      val myReporter = (rep: Reporter) ⇒ new Reporter {
        protected def info0(pos: Position, msg: String, severity: Severity, force: Boolean): Unit =
          info0M.invoke(rep, pos, msg, severity, force: java.lang.Boolean)

        override def echo(pos: Position, msg: String): Unit = {
          val p =
            msg match {
              case MessageFormat(what, forT, HasMatchingSymbolFormat(error)) ⇒
                // TODO: check if excluding all "type mismatches" makes sense
                if (!error.startsWith("type mismatch")) HasMatchingSymbolError(what, forT, error)
                else UnknownProblem(msg)
              case MessageFormat(what, forT, error) ⇒ OtherError(what, forT, error)
              case x                                ⇒ UnknownProblem(x)
            }
          problems ::= p
        }
      }

      val result =
        withSetting(settings.XlogImplicits, true) {
          withMyReporter(myReporter) {
            c.inferImplicitValue(tpe, silent = true, withMacrosDisabled = true)
          }
        }

      val infos = problems.flatMap {
        case p: NotAValidImplicitValueForProblem ⇒ Some(ProblemInfo(p.what, p.forT, p.message))
        case _                                   ⇒ None
      }.distinct.sortBy(problemRank)
      Some(ImplicitSearch(tpe.toString, infos)).filter(s ⇒ result.isEmpty && s.problems.nonEmpty)
    }
  }
}
