package part1_recap

import akka.actor.ActorSystem
import akka.stream.{ActorMaterializer, OverflowStrategy}
import akka.stream.scaladsl.{Flow, Sink, Source}

import scala.util.{Failure, Success}

object AkkaStreamRecap extends App {

  implicit val system = ActorSystem("AkkaStreamRecap")
  implicit val materializer = ActorMaterializer()
  import system.dispatcher

  val source = Source(1 to 1000) //the data source
  val sink = Sink.foreach[Int](println) // the final processing of the data
  val flow = Flow[Int].map(x => x * 1000) //intermediate processing

  //I find it similar to Kafka Streams/KSQL??? similar concept maybe
  val runnableGraph = source.via(flow).to(sink)
  val simpleMaterializerValue = runnableGraph.run()

  //MATERIALIZED VALUE
  //Seems like a way to obtain values from an operation that runs trough the source and returns the value in a Future
  val sumSink = Sink.fold[Int,Int](0)((currentSum, element) => currentSum + element)
  val sumFuture = source.runWith(sumSink)

  sumFuture.onComplete {
    case Success(sum) => println(s"The total sum is $sum")
    case Failure(ex) => println(s"Sum of all values failed $ex")
  }

  //Running stuff with buffers to delay or drop values in case of high volume it seems
  val bufferedFlow = Flow[Int].buffer(10, OverflowStrategy.dropHead)
  source.async.via(bufferedFlow).async.runForeach {
    e =>
    Thread.sleep(100)
    println(s"SOME LONG COMPUTATION $e")
  }





}
