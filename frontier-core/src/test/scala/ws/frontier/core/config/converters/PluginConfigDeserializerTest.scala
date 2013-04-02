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
    FrontierMapper.readValue(json, classOf[PluginConfig])
  }

  it should "deserialize array instances" in {
    val json = "[]"
    val configs: Array[PluginConfig] = FrontierMapper.readValue(json, classOf[Array[PluginConfig]])
    configs should not(be(null))
    configs.length should be(0)
  }
}