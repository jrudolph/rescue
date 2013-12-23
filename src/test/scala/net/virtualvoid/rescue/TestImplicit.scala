package net.virtualvoid.rescue

import spray.json.{ RootJsonReader, RootJsonFormat, DefaultJsonProtocol, JsonFormat }
import scala.concurrent.Future
import spray.httpx.marshalling.Marshaller
import spray.http.{ BodyPart, HttpForm }

//JsonOutput

class TestImplicit {
  import net.virtualvoid.rescue.HelpMe.withMyImplicits

  //implicitly[JsonFormat[Seq[String]]]
  import spray.httpx.SprayJsonSupport._
  import scala.concurrent.ExecutionContext.Implicits.global
  import spray.json.DefaultJsonProtocol._

  trait Gustav
  implicit val xyz: JsonFormat[Gustav] = null

  object Abcd extends DefaultJsonProtocol
  //import Abcd._
  //implicitly[Marshaller[Future[Seq[Gustav]]]]

  //implicit val x: Option[BodyPart] ⇒ Gustav = null

  import spray.routing.Directives._
  formFields('x.as[Gustav]) { (t) ⇒
    ???
  }
}
