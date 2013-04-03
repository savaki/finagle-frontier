package ws.frontier.core.config.converters

import ws.frontier.test.TestSuite
import ws.frontier.core.config.{FrontierConfig, FrontierMapper}
import java.io.File

/**
 * @author matt
 */

class FrontierConfigDeserializerTest extends TestSuite {
  "#deserialize" should "parse frontier config" in {
    val file: File = new File("frontier-core/src/test/resources/config/basic.json")
    val config: FrontierConfig = FrontierMapper.readValue[FrontierConfig](file)
    config should not(be(null))
  }
}
