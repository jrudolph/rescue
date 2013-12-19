package net.virtualvoid.wmmcc

import spray.json.{DefaultJsonProtocol, JsonFormat}
import scala.concurrent.Future
import spray.httpx.marshalling.Marshaller

class TestImplicit {
  //Scasandra.`please don't cry but tell what's wrong with this *!&%!$&`[JsonFormat[Seq[String]]]
  //implicitly[JsonFormat[Seq[String]]]
  import spray.httpx.SprayJsonSupport._
  import scala.concurrent.ExecutionContext.Implicits.global
  import spray.json.DefaultJsonProtocol._

  trait Gustav
  val xyz: JsonFormat[Gustav] = null

  object Abc extends DefaultJsonProtocol
  import Abc._
  //Scasandra.`please don't cry but tell what's wrong with this *!&%!$&`[Marshaller[Future[Seq[String]]]]
  import Scasandra.xxx
  implicitly[Marshaller[Future[Seq[Gustav]]]]
}
