package ws.frontier.core

import config.{FrontierConfig, FrontierMapper}
import java.net.URL

/**
 * @author matt
 */

class Frontier {
  def start() {
    val resource: URL = getClass.getClassLoader.getResource("frontier.json")
    val config: FrontierConfig = FrontierMapper.readValue(resource)

    config.validate()
  }
}