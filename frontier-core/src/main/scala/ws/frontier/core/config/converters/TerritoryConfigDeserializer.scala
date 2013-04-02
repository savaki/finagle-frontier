package ws.frontier.core.config.converters

import com.fasterxml.jackson.databind.{JsonNode, DeserializationContext, JsonDeserializer}
import ws.frontier.core.config.TerritoryConfig
import com.fasterxml.jackson.core.JsonParser

/**
 * @author matt
 */

class TerritoryConfigDeserializer extends JsonDeserializer[TerritoryConfig] with Deserializer[TerritoryConfig] {
  def deserialize(parser: JsonParser, context: DeserializationContext): TerritoryConfig = {
    println("TerritoryConfigDeserializer#deserialize")
    readNode(parser.readValueAsTree()).head
  }

  protected def readValue(node: JsonNode): TerritoryConfig = {
    null
  }
}