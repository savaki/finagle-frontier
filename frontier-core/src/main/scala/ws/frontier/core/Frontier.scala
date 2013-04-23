package ws.frontier.core

import com.fasterxml.jackson.annotation.JsonProperty
import com.twitter.util.Future
import java.util.{Map => JMap, HashMap => JHashMap}
import org.joda.time.DateTime
import scala.beans.BeanProperty
import scala.collection.JavaConversions._
import ws.frontier.core.template.{PassThroughTemplateFactory, TemplateFactory}
import ws.frontier.core.util.{Logging, Banner}

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

  /**
   * #withTrails allows this Frontier instance's #trails values to be assigned from a Java Map.
   *
   * @param trails a Java Map that contains trails by ids that we want to inject
   * @return this instance with the trails assigned
   */
  private[core] def withTrails(trails: JMap[String, _]): Frontier[IN, OUT] = {
    this.trails = trails
      .map(entry => entry._1 -> entry._2).toMap
      .asInstanceOf[Map[String, Trail[IN, OUT]]]
    this
  }

  /**
   * retrieves the decorator with the name specified or returns null if no matching decorator was found
   *
   * @param name the name of the decorator we're searching for
   * @return the optional decorator
   */
  def decorator(name: String): Option[Decorator] = {
    Option(decorators.get(name))
  }

  /**
   * find the trail with the id specified
   *
   * @param id the id of the trail
   * @return the trail that you're looking for
   */
  def trail(id: String): Option[Trail[IN, OUT]] = {
    if (id == null) {
      Some(territories.head.trail)
    } else {
      trails.get(id)
    }
  }

  protected def eachTerritory(function: Territory[IN, OUT] => Future[Unit]): Future[Unit] = {
    if (territories == null) {
      territories = Array()
      Future()

    } else {
      Future.collect {
        territories.map(function)
      }.map {
        values => ()
      }
    }
  }

  /**
   * @param log the buffer to write the banner message back to
   */
  def banner(log: Banner) {
    log("Frontier started at %s" format (new DateTime().toString("MM/dd/yyyy HH:mm:ss")))

    log()
    log("Options:")
    log.child {
      options.banner(log)
    }

    log()
    log("Territories:")
    log.child {
      territories.foreach(_.banner(log))
    }

    if (decorators != null) {
      log()
      log("Decorators:")
      log.child {
        decorators.values().foreach(_.banner(log))
      }
    }
  }

  def initialize(options: FrontierOptions): Future[Unit] = {
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

    def initializeTerritoriesAndDecorators(): Future[Unit] = {
      if (decorators == null) {
        decorators = new JHashMap[String, Decorator]
      }

      // territories need to be initialized PRIOR to initializing the decorators
      eachTerritory(_.initialize(this)).map {
        unit => decorators.values().foreach(decorator => decorator.initialize(this, options))
      }
    }

    this.options = Option(this.options)
      .getOrElse(FrontierOptions()) // ensures we have a non-null FrontierOptions value
      .zip(options) // allow the provided options to override any values we may be using

    info("initializing Frontier")
    initializeTemplateFactories()
    initializeTerritoriesAndDecorators()
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


