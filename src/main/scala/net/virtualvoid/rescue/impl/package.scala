package net.virtualvoid.rescue

package object impl {
  implicit class RichInfo(val p: ProblemInfo) extends AnyVal {
    def missingImplicit: Option[String] = Analysis.missingImplicit(p.message)
  }
}
