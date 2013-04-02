package ws.frontier.core.config.converters

import ws.frontier.test.TestSuite
import ws.frontier.core.config.{PluginConfig, FrontierMapper}

/**
 * @author matt
 */

class PluginConfigDeserializerTest extends TestSuite {
  "#deserialize" should "deserialize simple plugin config" in {
    val json =
      """
        |{
        |  "name": "sample-filter",
        |  "kind": "filter",
        |  "class": "ws.frontier.plugin.SampleFilter"
        |}
      """.stripMargin
    FrontierMapper.readValue[PluginConfig](json)
  }
}