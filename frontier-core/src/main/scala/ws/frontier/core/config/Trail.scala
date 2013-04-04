package ws.frontier.core.config

import com.twitter.util.Future

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
}

/**
 * TrailAggregator provides a way to prioritize trail selection among a number of trails.  First trail to match wins.
 * If no trail matches, then None will be returned.
 *
 * @param trails the universe of potential options to choose from
 */
class AggregatingTrail[IN, OUT](trails: Trail[IN, OUT]*) extends Trail[IN, OUT] {
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
}

abstract class RoutableTrail[IN, OUT] extends Trail[IN, OUT] {
  def routingInfo: RoutingInfo
}

class RoutingTrail[IN, OUT](router: Router, trails: Trail[IN, OUT]*) extends Trail[IN, OUT] {
  /**
   * @return None if this trail cannot handled the provided request; Some(Future[OUT]) if the action was handled
   */
  def apply(request: IN): Option[Future[OUT]] = {
    //
  }
}

trait RoutingInfo

trait Router
