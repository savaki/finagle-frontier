package ws.frontier.core

import ws.frontier.test.TestSuite
import com.twitter.finagle.http.{Request, Response}
import ws.frontier.core.util.Banner

/**
 * @author matt
 */

class FrontierTest extends TestSuite {
  "#initialize" should "set #territories and #decorators" in {
    val frontier = new Frontier[Request, Response]()
    frontier.initialize(FrontierOptions())
    frontier.decorators should not be (null)
    frontier.territories should not be (null)
  }

  "#banner" should "should be callable once #initialize has been called" in {
    val frontier = new Frontier[Request, Response]()
    frontier.initialize(FrontierOptions())
    frontier.banner(new Banner)
  }
}