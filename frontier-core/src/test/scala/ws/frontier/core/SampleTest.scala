package ws.frontier.core

import ws.frontier.test.TestSuite
import ws.frontier.core.converter.FrontierMapper
import java.io.File
import com.twitter.finagle.http.{Request, Response}

/**
 * @author matt
 */

class SampleTest extends TestSuite {
  "#frontier" should "proxy services for multiple websites" in {
    val frontier = FrontierMapper.readValue[Frontier[Request, Response]](new File("frontier-core/src/test/resources/sample.json"))
    frontier.initialize()
  }
}