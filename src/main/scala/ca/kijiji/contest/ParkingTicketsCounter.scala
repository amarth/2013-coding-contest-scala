package ca.kijiji.contest

import akka.actor.{Props, ActorSystem, Actor}
import akka.event.Logging

import scala.io._
import java.io.InputStream
import scala.collection.JavaConversions._
import collection.mutable
import scala.concurrent.duration._

object ParkingTicketsCounter {

  val system = ActorSystem("System")


  val fineAccumulator = new mutable.HashMap[String, Integer]()
  val accumulator = system.actorOf(Props(new FineAccumulatorActor(fineAccumulator)))

  val actorsCount = math.max(1, Runtime.getRuntime().availableProcessors - 2)
  val parserActors = for {
    i <- 1 to actorsCount
  } yield system.actorOf(Props(new ChunkParserActor(accumulator, StreamFineRecordParser)))


  abstract class AccMsg

  case class ChunkSent(id: Int)

  case class ChunkProcessed(id: Int, result: List[Option[(String, Int)]])



  def sortStreetsByProfitability(parkingTicketsStream: InputStream): java.util.Map[String, Integer] = {

    var ind = 0
    Source.createBufferedSource(parkingTicketsStream, 8 * 1024).getLines().drop(1).grouped(2 * 1024)
      .foreach(chunk => {
        parserActors(ind % actorsCount) !(ind, chunk)
        accumulator ! ChunkSent(ind)
        ind += 1
    })

    system.awaitTermination(20 seconds)

    fineAccumulator
  }
}

