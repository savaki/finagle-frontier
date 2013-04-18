package ws.frontier.core

import com.twitter.util.Future
import java.util.{Map => JMap}
import org.joda.time.DateTime
import scala.beans.BeanProperty
import scala.collection.JavaConversions._
import ws.frontier.core.util.Banner

/**
 * @author matt.ho@gmail.com
 */
class Frontier[IN, OUT] extends Registry[IN, OUT] {
  @BeanProperty
  var decorators: JMap[String, Decorator] = null

  @BeanProperty
  var territories: Array[Territory[IN, OUT]] = null

  var trails: Map[String, Trail[IN, OUT]] = Map()

  private[core] def withTrails(trails: JMap[_, _]): Frontier[IN, OUT] = {
    this.trails = trails
      .map(entry => entry._1 -> entry._2).toMap
      .asInstanceOf[Map[String, Trail[IN, OUT]]]
    this
  }

  def decorator(name: String): Option[Decorator] = {
    Option(decorators.get(name))
  }

  def trail(id: String): Option[Trail[IN, OUT]] = {
    trails.get(id)
  }

  protected def eachTerritory(function: Territory[IN, OUT] => Future[Unit]): Future[Unit] = {
    Future.collect {
      territories.map(function)
    }.map {
      values => ()
    }
  }

  /**
   * @param log the buffer to write the banner message back to
   */
  def banner(log: Banner) {
    log("Frontier started at %s" format (new DateTime().toString("MM/dd/yyyy HH:mm:ss")))
    log()
    log("Territories:")
    log.child {
      territories.foreach(_.banner(log))
    }
    log()
    log("Decorators:")
    log.child {
      decorators.values().foreach(_.banner(log))
    }
  }

  def initialize(): Future[Unit] = {
    eachTerritory(_.initialize())
  }

  /**
   * @return the list of ports that we were bound to
   */
  def start(): Future[Seq[Int]] = {
    Future.collect {
      territories.map(_.start(this))
    }
  }

  def shutdown(): Future[Unit] = {
    eachTerritory(_.shutdown())
  }
}

