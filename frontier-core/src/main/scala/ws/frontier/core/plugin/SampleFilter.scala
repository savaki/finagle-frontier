package ws.frontier.core.plugin

import ws.frontier.core.Plugin
import ws.frontier.core.config.PluginConfig

/**
 * @author matt
 */

class SampleFilter[IN, OUT] extends Plugin[IN, OUT] {
  /**
   * #validate is the first method invoked on Plugin to ensure that the configuration provided is valid for the
   * specified plugin
   *
   * @param config the configuration to verify
   */
  def validate(config: PluginConfig) = {
    Array()
  }

  /**
   * once the plugin has been validated, initialize is invoked prior to usage
   *
   * @param config the configuration for the plugin
   */
  def initialize(config: PluginConfig) {

  }
}