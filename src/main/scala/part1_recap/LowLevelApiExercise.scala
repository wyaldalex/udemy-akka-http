package part1_recap

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.Http.IncomingConnection
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, HttpMethods, HttpRequest, HttpResponse, StatusCodes, Uri}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Flow, Sink}

import scala.concurrent.Future
import scala.util.{Failure, Success}

object LowLevelApiExercise extends App {

  implicit val system = ActorSystem("LowLevelAPISystem")
  implicit val materializer = ActorMaterializer()
  import system.dispatcher

  //Async trough Akka Streams
  val streamBasedRequestHandler: Flow[HttpRequest,HttpResponse, _] = Flow[HttpRequest].map {
    case HttpRequest(HttpMethods.GET, Uri.Path("/about"), _, _, _) => //method,URI, HTTP headers, content and protocol http1/http2
      HttpResponse(
        StatusCodes.OK,
        entity = HttpEntity(
          ContentTypes.`text/html(UTF-8)`,
          """
            |<html>
            |<body>
            |<h1>This is the About Page</h1>
            |</body>
            |</html>
            |""".stripMargin
        )
      )
    case HttpRequest(HttpMethods.GET,  Uri.Path("/"), _, _, _) => //method,URI, HTTP headers, content and protocol http1/http2
      HttpResponse(
        StatusCodes.OK,
        entity = HttpEntity(
          ContentTypes.`text/plain(UTF-8)`,
          """
            |front door welcome
            |""".stripMargin
        )
      )
    case request: HttpRequest =>
      request.discardEntityBytes()
      HttpResponse(
        StatusCodes.NotFound,
        entity = HttpEntity.Empty
      )
  }
  Http().bindAndHandle(streamBasedRequestHandler,"localhost", 8388)

}
