package ws.frontier.core

import com.twitter.util.Future
import scala.beans.BeanProperty
import java.util.{Map => JMap}

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

object Frontier {
  def main(args: Array[String]) {
    val json =
      """
        |{
        |   trails =
        |}
      """.stripMargin
  }
}
