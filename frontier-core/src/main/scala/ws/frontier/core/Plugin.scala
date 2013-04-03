package ws.frontier.core

import config.{ValidationError, PluginConfig}

/**
 * @author matt
 */

trait Plugin[IN, OUT] {
  /**
   * #validate is the first method invoked on Plugin to ensure that the configuration provided is valid for the
   * specified plugin
   *
   * @param config the configuration to verify
   */
  def validate(config: PluginConfig): Array[ValidationError]

  /**
   * once the plugin has been validated, initialize is invoked prior to usage
   *
   * @param config the configuration for the plugin
   */
  def initialize(config: PluginConfig)
}