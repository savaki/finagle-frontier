package ws.frontier.core

import ws.frontier.core.template.{PassThroughTemplateFactory, TemplateFactory}

/**
 * @author matt
 */

trait Registry[IN, OUT] {
  /**
   * @param name the name of the decorator we're searching for
   */
  def decorator(name: String): Option[Decorator]

  /**
   * find a trail by id.  as ids are optional on trails, you may not find what you're looking for
   *
   * @param id the id of the trail
   * @return the trail that you're looking for
   */
  def trail(id: String = null): Option[Trail[IN, OUT]]

  def templateFactories: Array[TemplateFactory]
}

class EmptyRegistry[IN, OUT] extends Registry[IN, OUT] {
  def decorator(name: String): Option[Decorator] = None

  def trail(id: String): Option[Trail[IN, OUT]] = {
    Option(new EmptyTrail[IN, OUT])
  }

  val templateFactories: Array[TemplateFactory] = Array(new PassThroughTemplateFactory)
}
