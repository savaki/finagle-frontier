package ws.frontier.core.config.converters

import ws.frontier.test.TestSuite
import ws.frontier.core.config.{PluginConfig, FrontierMapper}

/**
 * @author matt
 */

class PluginConfigDeserializerTest extends TestSuite {
  val valid =
    """
      |{
      |  "name": "sample-filter",
      |  "kind": "filter",
      |  "class": "ws.frontier.core.plugin.SampleFilter"
      |}
    """.stripMargin

  val invalidKlass =
    """
      |{
      |  "name": "sample-filter",
      |  "kind": "filter",
      |  "class": "this.is.Junk"
      |}
    """.stripMargin

  "#deserialize" should "deserialize simple plugin config" in {
    val config: PluginConfig = FrontierMapper.readValue[PluginConfig](valid)
    config should not(be(null))
    config.name should be("sample-filter")
    config.kind should be("filter")
    config.klass should be("ws.frontier.core.plugin.SampleFilter")
  }

  it should "deserialize array instances" in {
    val json = "[]"
    val configs: Array[PluginConfig] = FrontierMapper.readValue[Array[PluginConfig]](json)
    configs should not(be(null))
    configs.length should be(0)
  }

  "#validate" should "ensure the class exists" in {
    val config: PluginConfig = FrontierMapper.readValue[PluginConfig](valid)
    config.validate().isEmpty should be(true)
  }
}