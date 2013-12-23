package net.virtualvoid.rescue.impl

case class ProblemInfo(whatImplicit: String, forWhichType: String, message: String)
case class ImplicitSearch(lookingFor: String, problems: Seq[ProblemInfo])
