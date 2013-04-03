package ws.frontier.core.factory

import ws.frontier.core.config.PluginConfig
import ws.frontier.core.{Plugin, ServiceFactory}

/**
 * @author matt
 */

class PluginRegistry[IN, OUT] {
  def newService(config: PluginConfig): Plugin[IN, OUT] = {
    val plugin: Plugin[IN, OUT] = Class.forName(config.klass).newInstance().asInstanceOf[Plugin[IN, OUT]]
    plugin.initialize(config)
    null
  }
}