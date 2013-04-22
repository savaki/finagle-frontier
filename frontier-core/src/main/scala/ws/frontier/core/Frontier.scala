package ws.frontier.core

import com.twitter.util.Future
import java.util.{Map => JMap}
import org.joda.time.DateTime
import scala.beans.BeanProperty
import scala.collection.JavaConversions._
import ws.frontier.core.util.{Logging, Banner}
import ws.frontier.core.template.{PassThroughTemplateFactory, TemplateFactory}
import com.fasterxml.jackson.annotation.JsonProperty

/**
 * @author matt.ho@gmail.com
 */
class Frontier[IN, OUT] extends Registry[IN, OUT] with Logging {
  val DEFAULT_TEMPLATE_FACTORY: String = classOf[PassThroughTemplateFactory].getCanonicalName

  @BeanProperty
  var decorators: JMap[String, Decorator] = null

  @BeanProperty
  var territories: Array[Territory[IN, OUT]] = null

  @JsonProperty("template-factories")
  @BeanProperty
  var templateFactoriesKlass: Array[String] = null

  @BeanProperty
  var options: FrontierOptions = null

  var trails: Map[String, Trail[IN, OUT]] = Map()

  var templateFactories: Array[TemplateFactory] = null


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
    if (id == null) {
      Some(territories.head.trail)
    } else {
      trails.get(id)
    }
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

  def initialize(options: FrontierOptions): Future[Unit] = {
    this.options = Option(this.options)
      .getOrElse(FrontierOptions()) // ensures we have a non-null FrontierOptions value
      .zip(options) // allow the provided options to override any values we may be using

    def initializeTemplateFactories() {
      /**
       * instantiate the template factory to use.  for now, we only allow one template factory.  in future, we may allow
       * multiple template factories
       */
      templateFactories = Option(templateFactoriesKlass).getOrElse(Array[String]()).map {
        klass => {
          val factory: TemplateFactory = Class.forName(klass).newInstance().asInstanceOf[TemplateFactory]
          info("loading TemplateFactory: %s" format factory.name)
          factory
        }
      }
    }

    def initializeTerritories(): Future[Unit] = {
      // territories need to be initialized PRIOR to initializing the decorators
      eachTerritory(_.initialize(this)).map {
        unit => decorators.values().foreach(decorator => decorator.initialize(this, options))
      }
    }

    info("initializing Frontier")
    initializeTemplateFactories()
    initializeTerritories()
  }

  /**
   * @return the list of ports that we were bound to
   */
  def start(): Future[Seq[Int]] = {
    info("Frontier started at %s" format (new DateTime().toString("MM/dd/yyyy HH:mm:ss")))
    Future.collect {
      territories.map(_.start(this))
    }
  }

  def shutdown(): Future[Unit] = {
    info("Shutdown requested at %s" format (new DateTime().toString("MM/dd/yyyy HH:mm:ss")))
    eachTerritory(_.shutdown()).map {
      unit => info("Shutdown completed at %s" format (new DateTime().toString("MM/dd/yyyy HH:mm:ss")))
    }
  }
}


