package ws.frontier.core

/**
 * @author matt
 */

trait Registry[IN, OUT] {
  def decorator(name: String): Option[Decorator]

  def trail(id: String): Option[Trail[IN, OUT]]
}

class EmptyRegistry[IN, OUT] extends Registry[IN, OUT] {
  def decorator(name: String): Option[Decorator] = None

  def trail(id: String): Option[Trail[IN, OUT]] = None
}
