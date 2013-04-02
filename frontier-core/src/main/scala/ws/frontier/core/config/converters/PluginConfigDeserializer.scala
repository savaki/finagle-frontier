package ws.frontier.core.config.converters

import com.fasterxml.jackson.databind.{JsonNode, DeserializationContext, JsonDeserializer}
import ws.frontier.core.config.PluginConfig
import com.fasterxml.jackson.core.JsonParser

/**
 * @author matt
 */

class PluginConfigDeserializer extends JsonDeserializer[PluginConfig] with Deserializer[PluginConfig] {
  protected def readValue(node: JsonNode): PluginConfig = {
    val name = node.get("name").textValue()
    val kind = node.get("kind").textValue()
    val klass = node.get("class").textValue()
    PluginConfig(name, kind, klass)
  }

  def deserialize(parser: JsonParser, context: DeserializationContext): PluginConfig = {
    val node: JsonNode = parser.readValueAsTree()
    readNode(node).head
  }
}
