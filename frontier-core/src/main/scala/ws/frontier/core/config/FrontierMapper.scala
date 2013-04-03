package ws.frontier.core.config

import com.fasterxml.jackson.databind.ObjectMapper
import converters.{PluginConfigDeserializer, TerritoryConfigDeserializer, FrontierConfigDeserializer}
import com.fasterxml.jackson.databind.module.SimpleModule
import java.io.File
import java.net.URL

/**
 * @author matt
 */

class FrontierMapper {

  private[config] val plugin: PluginConfigDeserializer = new PluginConfigDeserializer

  private[config] val territory: TerritoryConfigDeserializer = new TerritoryConfigDeserializer

  private[config] val frontier: FrontierConfigDeserializer = new FrontierConfigDeserializer(plugin, territory)

  private[this] val mapper = {
    val m = new ObjectMapper

    val module = new SimpleModule
    module.addDeserializer(classOf[FrontierConfig], frontier)
    module.addDeserializer(classOf[TerritoryConfig], territory)
    module.addDeserializer(classOf[PluginConfig], plugin)
    m.registerModule(module)

    m
  }

  def readValue[T](json: String)(implicit t: Manifest[T]): T = {
    mapper.readValue(json, t.erasure.asInstanceOf[Class[T]])
  }

  def readValue[T](file: File)(implicit t: Manifest[T]): T = {
    mapper.readValue[T](file, t.erasure.asInstanceOf[Class[T]])
  }

  def readValue[T](url: URL)(implicit t: Manifest[T]): T = {
    mapper.readValue[T](url, t.erasure.asInstanceOf[Class[T]])
  }
}

object FrontierMapper extends FrontierMapper
