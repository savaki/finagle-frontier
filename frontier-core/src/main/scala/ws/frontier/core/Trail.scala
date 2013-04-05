package ws.frontier.core

import com.twitter.util.Future
import beans.BeanProperty

/**
 * Trails represent conditional executions of Finagle services.  Unlike a Finagle service which _must_ process calls to
 * #apply, a trail can optionally say that the request cannot be handled by itself by returning None
 *
 * @author matt
 */
abstract class Trail[IN, OUT] {
  /**
   * @return None if this trail cannot handled the provided request; Some(Future[OUT]) if the action was handled
   */
  def apply(request: IN): Option[Future[OUT]]

  @BeanProperty
  var tags: Array[String] = null

  def validate(): List[ValidationError]

  /**
   * @return a future that allows us to mark when initialization is complete
   */
  def initialize(): Future[Unit] = Future()

  def start(): Future[Unit]

  def shutdown(): Future[Unit]
}

/**
 * TrailAggregator provides a way to prioritize trail selection among a number of trails.  First trail to match wins.
 * If no trail matches, then None will be returned.
 *
 * @param trails the universe of potential options to choose from
 */
class AggregatingTrail[IN, OUT](val trails: Trail[IN, OUT]*) extends Trail[IN, OUT] {
  def apply(request: IN): Option[Future[OUT]] = {
    var index = 0
    while (index < trails.length) {
      val result = trails(index)(request)
      if (result.isDefined) {
        return result
      }
      index = index + 1
    }

    None
  }

  def validate() :List[ValidationError] = {
    trails.flatMap(_.validate()).toList
  }

  override def initialize(): Future[Unit] = {
    Future.join {
      trails.map(_.initialize())
    }
  }

  override def start(): Future[Unit] = {
    Future.join {
      trails.map(_.start())
    }
  }

  override def shutdown(): Future[Unit] = {
    Future.join {
      trails.map(_.start())
    }
  }
}

case class TrailGuide[IN, OUT](trail: Trail[IN, OUT], params: Map[String, String])

abstract class RoutingTrail[IN, OUT](guides: TrailGuide[IN, OUT]*) extends Trail[IN, OUT] {

}
