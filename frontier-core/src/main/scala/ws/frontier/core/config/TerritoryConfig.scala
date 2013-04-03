package ws.frontier.core.config

/**
 * @author matt
 */

case class TerritoryConfig(plugins: Array[PluginConfig] = Array(), services: Array[ServiceConfig] = Array()) extends Validating {
  def validate(): Array[ValidationError] = {
    null
  }
}

