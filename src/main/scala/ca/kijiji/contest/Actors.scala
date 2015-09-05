package ca.kijiji.contest

import akka.actor.{ActorRef, Actor}
import akka.event.Logging
import ca.kijiji.contest.ParkingTicketsCounter.{ChunkSent, ChunkProcessed}

import scala.collection.mutable


class ChunkParserActor(accumulator:ActorRef, fineRecordParser: FineRecordParser) extends Actor {
  val log = Logging(context.system, this)

  def receive = {
    case (id:Int, chunk: List[String]) =>
      accumulator ! ChunkProcessed (id, chunk map fineRecordParser.parseRecord)
    case _ => log.info("not a chunk tuple")
  }
}

class FineAccumulatorActor(fineAccumulator:mutable.HashMap[String, Integer]) extends Actor {

  val log = Logging(context.system, this)

  val chunkIds = new mutable.HashSet[Integer]()

  def receive = {
    case ChunkProcessed(id, fineInfo) =>
      fineInfo.foreach {
        _ match {
          case Some(fineStreet) =>
            fineAccumulator(fineStreet._1) = fineAccumulator.getOrElseUpdate(fineStreet._1, 0) + fineStreet._2
          case None => ()
        }
      }
      chunkIds.remove(id)
      if(chunkIds.isEmpty) { context.system.shutdown() }
    case ChunkSent(id) =>
      chunkIds.add(id)
    case x => log.info(x.toString)
  }
}