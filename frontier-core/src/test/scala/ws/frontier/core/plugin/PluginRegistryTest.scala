package ws.frontier.core.plugin

import ws.frontier.test.TestSuite

/**
 * @author matt.ho@gmail.com
 */
class PluginRegistryTest extends TestSuite {
  "#currentPlugins" should "contain the default plugins if outside the #withPlugins context" in {
    PluginRegistry.currentPlugins should be(PluginRegistry.defaultPlugins)
  }

  "#withPlugins" should "overwrite existing plugins" in {
    val expected: String = "new values"
    val plugins = Map("default" -> expected)
    var executed = false

    // Test 1 - non-overridden values
    PluginRegistry.currentPlugins("default") should not(be(expected))

    // Test 2 - override
    PluginRegistry.withPlugins(plugins) {
      PluginRegistry.currentPlugins("default") should be(expected)
      executed = true
    }
    executed should be(true)
  }
}
