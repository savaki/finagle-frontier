package ws.frontier.core.config

import java.io.Serializable

/**
 * @author matt
 */

case class FrontierConfig(plugins: Array[PluginConfig] = Array(), territories: Array[TerritoryConfig] = Array()) extends Serializable

