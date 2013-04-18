package ws.frontier.core

/**
 * @author matt
 */

trait Registry {
  def decorator(name: String): Option[Decorator]

  def trail[IN, OUT](id: String): Option[Trail[IN, OUT]]
}
