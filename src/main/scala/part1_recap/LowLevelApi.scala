package part1_recap

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.Http.IncomingConnection
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, HttpMethods, HttpRequest, HttpResponse, StatusCodes, Uri}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Flow, Sink}

import scala.concurrent.Future
import scala.util.{Failure, Success}

object LowLevelApi extends App {

  implicit val system = ActorSystem("LowLevelAPISystem")
  implicit val materializer = ActorMaterializer()
  import system.dispatcher

  val serverSource = Http().bind("localhost", 8089)
  val connectionSink = Sink.foreach[IncomingConnection] { connection =>
    println(s"Accepted incoming connection from ${connection.remoteAddress}")
  }

  val serverBindingFuture = serverSource.to(connectionSink).run()
  serverBindingFuture.onComplete {
    case Success(_) => println("Server binding succesful")
    case Failure(ex) => println(s"Server binding failed $ex")
  }

  //Synchronus request
  val requestHandler: HttpRequest => HttpResponse = {
    case HttpRequest(HttpMethods.GET, _, _, _, _) =>
      HttpResponse(
        StatusCodes.OK,
        entity = HttpEntity(
          ContentTypes.`text/html(UTF-8)`,
          """
            |<html>
            |<body>
            |<h1>Hello from Akka Http</h1>
            |</body>
            |</html>
            |""".stripMargin
        )
      )
    case request: HttpRequest =>
      request.discardEntityBytes()
      HttpResponse(
        StatusCodes.NotFound,
        entity = HttpEntity(
          ContentTypes.`text/html(UTF-8)`,
          """
            |<html>
            |<body>
            |<h1>OOOOOOPPPPPS EXPLOSION NOT FOUNNDD</h1>
            |</body>
            |</html>
            |""".stripMargin
        )
      )

  }

  val httpSyncConnectionHandler = Sink.foreach[IncomingConnection]{ connection =>
    connection.handleWithSyncHandler(requestHandler)
  }
  Http().bind("localhost", 9090).runWith(httpSyncConnectionHandler)


  //Asynchronous request
  val asyncRequestHandler: HttpRequest => Future[HttpResponse] = {
    case HttpRequest(HttpMethods.GET, Uri.Path("/home"), _, _, _) => //method,URI, HTTP headers, content and protocol http1/http2
      Future(HttpResponse(
        StatusCodes.OK,
        entity = HttpEntity(
          ContentTypes.`text/html(UTF-8)`,
          """
            |<html>
            |<body>
            |<h1>Hello from Akka Http</h1>
            |</body>
            |</html>
            |""".stripMargin
        )
      ))
    case request: HttpRequest =>
      request.discardEntityBytes()
      Future(HttpResponse(
        StatusCodes.NotFound,
        entity = HttpEntity(
          ContentTypes.`text/html(UTF-8)`,
          """
            |<html>
            |<body>
            |<h1>OOOOOOPPPPPS EXPLOSION NOT FOUNNDD</h1>
            |</body>
            |</html>
            |""".stripMargin
        )
      ))
  }
  val httpAsyncConnectionHandler = Sink.foreach[IncomingConnection]{ connection =>
    connection.handleWithAsyncHandler(asyncRequestHandler)
  }
  Http().bind("localhost", 9091).runWith(httpAsyncConnectionHandler)

  //Async trough Akka Streams
  val streamBasedRequestHandler: Flow[HttpRequest,HttpResponse, _] = Flow[HttpRequest].map {
    case HttpRequest(HttpMethods.GET, Uri.Path("/home"), _, _, _) => //method,URI, HTTP headers, content and protocol http1/http2
      HttpResponse(
        StatusCodes.OK,
        entity = HttpEntity(
          ContentTypes.`text/html(UTF-8)`,
          """
            |<html>
            |<body>
            |<h1>Hello from Akka Http with Streams</h1>
            |</body>
            |</html>
            |""".stripMargin
        )
      )
    case request: HttpRequest =>
      request.discardEntityBytes()
      HttpResponse(
        StatusCodes.NotFound,
        entity = HttpEntity(
          ContentTypes.`text/html(UTF-8)`,
          """
            |<html>
            |<body>
            |<h1>OOOOOOPPPPPS EXPLOSION NOT FOUNNDD</h1>
            |</body>
            |</html>
            |""".stripMargin
        )
      )
  }
  Http().bindAndHandle(streamBasedRequestHandler,"localhost", 9092)

}
