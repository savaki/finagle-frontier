package ws.frontier.core.config

/**
 * @author matt
 */

case class FrontierConfig(plugins: Array[PluginConfig] = Array(), territories: Array[TerritoryConfig] = Array()) extends Validating {
  def validate(): Array[ValidationError] = {
    plugins.flatMap(_.validate()) ++ territories.flatMap(_.validate())
  }
}

