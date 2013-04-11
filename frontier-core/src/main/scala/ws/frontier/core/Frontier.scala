package ws.frontier.core

import com.twitter.util.Future
import scala.beans.BeanProperty
import java.util.{Map => JMap, Date}
import org.joda.time.DateTime

/**
 * @author matt.ho@gmail.com
 */
class Frontier[IN, OUT] {
  @BeanProperty
  var decorators: JMap[String, Decorator] = null

  @BeanProperty
  var territories: Array[Territory[IN, OUT]] = null

  protected def eachTerritory(function: Territory[IN, OUT] => Future[Unit]): Future[Unit] = {
    Future.collect {
      territories.map(function)
    }.map {
      values => ()
    }
  }

  /**
   * @param io the buffer to write the banner message back to
   */
  def banner(log: Banner) {
    log("Frontier started at %s" format (new DateTime().toString("MM/dd/yyyy HH:mm:ss")))
    log()
    log("Territories:")
    log.child {
      territories.foreach(_.banner(log))
    }
  }

  def initialize(): Future[Unit] = {
    eachTerritory(_.initialize())
  }

  def start(): Future[Unit] = {
    eachTerritory(_.start())
  }

  def shutdown(): Future[Unit] = {
    eachTerritory(_.shutdown())
  }
}

