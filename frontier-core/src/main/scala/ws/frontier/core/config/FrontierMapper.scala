package ws.frontier.core.config

import com.fasterxml.jackson.databind.{JsonNode, ObjectMapper}
import converters.{PluginConfigDeserializer, TerritoryConfigDeserializer, FrontierConfigDeserializer}
import java.io.File

/**
 * @author matt
 */

object FrontierMapper {

  private[this] val plugin: PluginConfigDeserializer = new PluginConfigDeserializer

  private[this] val territory: TerritoryConfigDeserializer = new TerritoryConfigDeserializer

  private[this] val frontier: FrontierConfigDeserializer = new FrontierConfigDeserializer(plugin, territory)

  private[this] val mapper = new ObjectMapper()

  def readValue[T](content: String)(implicit t: Manifest[T]): T = {
    val node: JsonNode = mapper.readTree(content)
    readNode(node)(t)
  }

  def readValue[T](file: File)(implicit t: Manifest[T]): T = {
    val node: JsonNode = mapper.readTree(file)
    readNode(node)(t)
  }

  private def readNode[T](node: JsonNode)(implicit t: Manifest[T]): T = {
    if (t.erasure == classOf[FrontierConfig]) {
      frontier.readNode(node).head.asInstanceOf[T]

    } else if (t.erasure == classOf[Array[FrontierConfig]]) {
      frontier.readNode(node).head.asInstanceOf[T]

    } else if (t.erasure == classOf[PluginConfig]) {
      plugin.readNode(node).head.asInstanceOf[T]

    } else if (t.erasure == classOf[Array[PluginConfig]]) {
      plugin.readNode(node).head.asInstanceOf[T]

    } else {
      throw new UnsupportedOperationException("#readValue - unhandled type, %s" format t.erasure.getCanonicalName)
    }
  }
}
