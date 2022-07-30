package part3_highlevelapi

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, StatusCodes}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer

object HighLevelIntro extends App {

  implicit val system = ActorSystem("HighLevelIntro")
  implicit val materializer = ActorMaterializer()
  import system.dispatcher

  //directives

  val simpleRoute: Route = {
    path("home") {
      complete(StatusCodes.OK)
    }
  }

  val pathGetRoute: Route = {
    path("myEndPoint") {
      get{
        complete(StatusCodes.OK)
      } ~ //Tilde is extremly important as it chains the expresions, remember that its a expresion block, so the last expresion is the final vlaue
      post {
        complete(StatusCodes.Forbidden)
      }
    } ~
      path("home") {
        get{
          complete(
            HttpEntity(
              ContentTypes.`text/html(UTF-8)`,
              """
                |<html>
                | <body>
                |    Hello from High Level Akka HTTP!
                | </body>
                |</html>
                |""".stripMargin
            )
          )
        }
      }
  }

 //HTTP: Http().bindAndHandle(pathGetRoute,"localhost",8089)
  //HTTPS:
  import httpssetup.HttpsSetup.getHttpsContext
  Http().bindAndHandle(pathGetRoute,"localhost",8089, getHttpsContext)

}
