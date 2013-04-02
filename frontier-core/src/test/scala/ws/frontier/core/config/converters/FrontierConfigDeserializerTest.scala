package ws.frontier.core.config.converters

import ws.frontier.test.TestSuite
import ws.frontier.core.config.{FrontierConfig, FrontierMapper}
import java.io.File

/**
 * @author matt
 */

class FrontierConfigDeserializerTest extends TestSuite {
  "#deserialize" should "parse frontier config" in {
    val config: FrontierConfig = FrontierMapper.readValue[FrontierConfig](new File("frontier-core/src/test/resources/config/basic.json"))
    config should not(be(null))
  }
}
