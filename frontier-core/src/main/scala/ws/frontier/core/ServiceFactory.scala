package ws.frontier.core

import config.PluginConfig
import com.twitter.finagle.Service

/**
 * @author matt
 */

trait ServiceFactory[IN, OUT] {
  def validate(config: PluginConfig)

  def newInstance(config: PluginConfig): Service[IN, OUT]
}