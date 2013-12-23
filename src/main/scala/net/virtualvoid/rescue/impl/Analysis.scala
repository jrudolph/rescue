package net.virtualvoid.rescue.impl

sealed trait Tree {
  def info: ProblemInfo
  def children: Seq[Tree]
  def leafProblems: Seq[ProblemInfo]
}
case class Branch(info: ProblemInfo, children: Seq[Tree]) extends Tree {
  def leafProblems = children.flatMap(_.leafProblems).distinct
}
case class Leaf(info: ProblemInfo) extends Tree {
  def leafProblems: Seq[ProblemInfo] = Seq(info)
  def children: Seq[Tree] = Nil
}

object Analysis {
  val DefaultEvidenceMsg = """could not find implicit value for evidence parameter of type (.*)$""".r
  val DefaultParameterMsg = """could not find implicit value for parameter [^:]+: (.*)$""".r
  val DefaultViewMsg = """No implicit view available from (.*)\.$""".r
  val ExecutionContextMsg = "Cannot find an implicit ExecutionContext, either require one yourself or import ExecutionContext.Implicits.global"
  val RootJsonWriterMsg = """Cannot find RootJsonWriter or RootJsonFormat type class for (.*)$""".r
  val RootJsonReaderMsg = """Cannot find RootJsonReader or RootJsonFormat type class for (.*)$""".r

  /** Extract target types from well-known error messages (from @implicitNotFound) */
  def missingImplicit(message: String): Option[String] = message match {
    case ExecutionContextMsg      ⇒ Some("scala.concurrent.ExecutionContext")
    case DefaultEvidenceMsg(tpe)  ⇒ Some(tpe)
    case DefaultParameterMsg(tpe) ⇒ Some(tpe)
    case DefaultViewMsg(tpe)      ⇒ Some(tpe)
    case RootJsonWriterMsg(tpe)   ⇒ Some(s"spray.json.RootJsonWriter[$tpe]")
    case RootJsonReaderMsg(tpe)   ⇒ Some(s"spray.json.RootJsonReader[$tpe]")
    case _ ⇒
      //println(s"'$message' didn't match")
      None
  }
  // FIXME: unify nodes with identical target type but different implicit
  def reconstructSearchTree(search: ImplicitSearch): Tree = {
    case class ChildParent(info: ProblemInfo, child: Option[String], target: String)

    val relations =
      for {
        info ← search.problems
      } yield ChildParent(info, info.missingImplicit, info.forWhichType)

    def tree(rel: ChildParent): Tree = {
      val children = relations.filter(x ⇒ rel.child.exists(_ == x.target))
      if (children.isEmpty) Leaf(rel.info)
      else Branch(rel.info, children.map(tree))
    }

    val rootInfo = ChildParent(ProblemInfo("", search.lookingFor, ""), Some(search.lookingFor), "")
    tree(rootInfo)
  }
}
