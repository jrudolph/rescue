package net.virtualvoid.rescue

import scala.language.experimental.macros

import impl.ImplicitsMacros

object HelpMe {
  implicit def withMyImplicits[T]: T = macro ImplicitsMacros.rootCauses[T]
  implicit def withMyImplicitsNoAnsi[T]: T = macro ImplicitsMacros.rootCausesNoAnsi[T]
}
