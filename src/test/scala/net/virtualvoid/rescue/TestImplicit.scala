package net.virtualvoid.rescue

import spray.json.{DefaultJsonProtocol, JsonFormat}
import scala.concurrent.Future

class TestImplicit {
  // remove comment to enable analysis
  // import net.virtualvoid.rescue.HelpMe.withMyImplicits

  import spray.httpx.SprayJsonSupport._
  // remove comment to fix
  // import scala.concurrent.ExecutionContext.Implicits.global
  import spray.json.DefaultJsonProtocol._

  trait Banana
  implicit val xyz: JsonFormat[Banana] = null

  // comment out to fix ambiguous implicits error
  object Abcd extends DefaultJsonProtocol
  import Abcd._

  import spray.routing.Directives._

  def x: Future[Seq[Banana]] = null

  complete(x)
}
