package ws.frontier.core.config.converters

import com.fasterxml.jackson.databind.{JsonNode, DeserializationContext, JsonDeserializer}
import ws.frontier.core.config.{PluginConfig, TerritoryConfig, FrontierConfig}
import com.fasterxml.jackson.core.JsonParser

/**
 * @author matt
 */

class FrontierConfigDeserializer(plugin: PluginConfigDeserializer, territory: TerritoryConfigDeserializer) extends JsonDeserializer[FrontierConfig] with Deserializer[FrontierConfig] {
  def deserialize(parser: JsonParser, context: DeserializationContext): FrontierConfig = {
    val node: JsonNode = parser.readValueAsTree()
    readNode(node).head
  }

  protected def readValue(node: JsonNode): FrontierConfig = {
    val territories: Array[TerritoryConfig] = territory.readNode(node.get("territories"))
    val plugins: Array[PluginConfig] = plugin.readNode(node.get("plugins"))
    FrontierConfig(plugins, territories)
  }
}
