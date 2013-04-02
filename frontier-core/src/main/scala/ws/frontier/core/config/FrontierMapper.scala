package ws.frontier.core.config

import com.fasterxml.jackson.databind.ObjectMapper
import converters.{PluginConfigDeserializer, TerritoryConfigDeserializer, FrontierConfigDeserializer}
import com.fasterxml.jackson.databind.module.SimpleModule

/**
 * @author matt
 */

class FrontierMapper extends ObjectMapper {

  private[this] val plugin: PluginConfigDeserializer = new PluginConfigDeserializer

  private[this] val territory: TerritoryConfigDeserializer = new TerritoryConfigDeserializer

  private[this] val frontier: FrontierConfigDeserializer = new FrontierConfigDeserializer(plugin, territory)

  private[this] val module = {
    val m = new SimpleModule
    m.addDeserializer(classOf[FrontierConfig], frontier)
    m.addDeserializer(classOf[TerritoryConfig], territory)
    m.addDeserializer(classOf[PluginConfig], plugin)
    m
  }

  registerModule(module)
}

object FrontierMapper extends FrontierMapper
