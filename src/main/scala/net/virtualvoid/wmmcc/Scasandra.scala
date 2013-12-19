package net.virtualvoid.wmmcc

import scala.language.experimental.macros

import scala.reflect.macros.{ TypecheckException, Context }
import scala.tools.nsc.reporters.Reporter
import scala.reflect.internal.util.Position

object Scasandra {
  def `please don't cry but tell what's wrong with this *!&%!$&`[T]: T = macro ScasandraMacro.`I'm feeling desperate`[T]

  implicit def xxx[T]: T = macro ScasandraMacro.`I'm feeling desperate`[T]
}

object ScasandraMacro {
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
  //'marshalling.this.Marshaller.mMarshaller is not a valid implicit value for spray.httpx.marshalling.Marshaller[Seq[String]] because:
  //hasMatchingSymbol reported error: could not find implicit value for parameter mm: spray.httpx.marshalling.MarshallerM[Seq]
  val MessageFormat = s"""(?s:(.*?) is not a valid implicit value for (.*?) because:${"\n"}(.*))""".r
  val HasMatchingSymbolFormat = """hasMatchingSymbol reported error: (.*)""".r

  def `I'm feeling desperate`[T: c.WeakTypeTag](c: Context): c.Expr[T] = {
    val tpe = c.openImplicits.head._1 //c.weakTypeTag[T]

    //println("Was called " + c.openImplicits.size + " for type " + tpe)
    if (c.openImplicits.size > 1) {
      c.error(c.enclosingPosition, "Recursive call")
      throw new RuntimeException
    } else println(c.openImplicits, c.openMacros)

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

    sealed trait Problem
    sealed trait NotAValidImplicitValueForProblem extends Problem {
      def what: String
      def forT: String
      def message: String
    }
    case class HasMatchingSymbolError(what: String, forT: String, message: String) extends NotAValidImplicitValueForProblem
    case class OtherError(what: String, forT: String, message: String) extends NotAValidImplicitValueForProblem
    case class UnknownProblem(msg: String) extends Problem

    var problems: List[Problem] = Nil

    val myReporter = (rep: Reporter) ⇒ new Reporter {
      protected def info0(pos: Position, msg: String, severity: Severity, force: Boolean): Unit =
        info0M.invoke(rep, pos, msg, severity, force: java.lang.Boolean)

      override def echo(pos: Position, msg: String): Unit = {
        val p =
          msg match {
            case MessageFormat(what, forT, HasMatchingSymbolFormat(error)) ⇒ HasMatchingSymbolError(what, forT, error)
            case MessageFormat(what, forT, error) ⇒ OtherError(what, forT, error)
            case x ⇒ UnknownProblem(x)
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
    def problemRank(p: Problem) = p match {
      case x: NotAValidImplicitValueForProblem ⇒ (0, x.forT.length)
      case UnknownProblem(msg)                 ⇒ (1, msg.length)
    }

    val message = "Problems found during implicit search:\n" + problems.distinct.sortBy(problemRank).map {
      case p: NotAValidImplicitValueForProblem ⇒
        f"${p.forT}%-80s ${p.what}%-50s ${p.message}"
      case UnknownProblem(msg) ⇒ msg
    }.mkString("\n")
    //c.error(c.enclosingPosition, message)
    if (result.isEmpty) {
      c.echo(c.universe.NoPosition, message)
      c.error(c.enclosingPosition, message)
    }

    c.universe.reify {
      null.asInstanceOf[T]
    }
  }
}
