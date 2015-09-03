package ca.kijiji.contest

import akka.actor.{Props, ActorSystem, Actor}
import akka.event.Logging

import scala.io._
import java.io.InputStream
import scala.collection.JavaConversions._
import collection.mutable
import scala.concurrent.duration._

object ParkingTicketsCounter {


  val fineAccumulator = new mutable.HashMap[String, Integer]()

  val system = ActorSystem("System")
  val parsers = for{i <- 1 to 6} yield system.actorOf(Props(new ChunkParserActor()))
  val accumulator = system.actorOf(Props(new FineAccumulatorActor()))


  abstract class AccMsg
  case class ChunkSent(id:Int)
  case class ChunkProcessed(id:Int, result:List[(String, Int)])


  def sortStreetsByProfitability(parkingTicketsStream: InputStream): java.util.SortedMap[String, Integer] = {

    var ind = 0
    Source.createBufferedSource(parkingTicketsStream, 8 * 1024).getLines().drop(1).grouped(2*1024)
    .foreach(chunk => {parsers(ind%6) ! (ind, chunk); accumulator ! ChunkSent(ind); ind+=1})

    system.awaitTermination(20 seconds)

    val result: ValueSortedMap[String, Integer] =
      new ValueSortedMap[String, Integer](new MapValueComparator[String, Integer]())
    result.putAll(fineAccumulator)
    result
  }

  val streetTypes = Set("AVE", "CRT", "CIR", "TER", "WAY", "BLVD")
  def parseRecord(line: String): (String, Int) = {
    val recordParts: Array[String] = line.split(',')

    val fine = recordParts(4).toInt

    val addressParts = recordParts(7)
      .split(" ")
      .filterNot(p => {
      p.length < 2 || (p.charAt(0) <= '9' && p.charAt(0) >= '0')
    }).toList

    val street: String =
      if (addressParts == Nil)
        "error"
      else if (addressParts.length == 1)
        addressParts.head
      else
        (addressParts.head :: addressParts.tail.filterNot(p => p.length < 3 || streetTypes.contains(p))).mkString(" ")

    (street, fine)
  }

  class ChunkParserActor extends Actor {
    val log = Logging(context.system, this)

    def receive = {
      case (id:Int, chunk: List[String]) =>
        accumulator ! ChunkProcessed (id, chunk map parseRecord)
      case _ => log.info("not a chunk tuple")
    }
  }

  class FineAccumulatorActor extends Actor {
    val log = Logging(context.system, this)

    val chunkIds = new mutable.HashSet[Integer]()

    def receive = {
      case ChunkProcessed(id, fineInfo) =>
        fineInfo.foreach {
          streetFine:(String, Int) =>
            fineAccumulator(streetFine._1) = fineAccumulator.getOrElseUpdate(streetFine._1, 0) + streetFine._2
        }
        chunkIds.remove(id)
        if(chunkIds.isEmpty) { context.system.shutdown() }
      case ChunkSent(id) =>
        chunkIds.add(id)
      case x => log.info(x.toString)
    }
  }

}

