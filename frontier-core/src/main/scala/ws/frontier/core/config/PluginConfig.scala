package ws.frontier.core.config

/**
 * @author matt
 */

case class PluginConfig(name: String, kind: String, klass: String, params: Map[String, String] = Map())
