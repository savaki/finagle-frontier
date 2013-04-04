package ws.frontier.core.util

import ws.frontier.test.TestSuite
import java.util
import util.Collections
import com.twitter.util.Future

/**
 * @author matt.ho@gmail.com
 */
class FuturesTest extends TestSuite {
  "#apply" should "not kill threads when exceptions are thrown" in {
    val invoked = Collections.synchronizedSet(new util.HashSet[Int])
    val count: Int = 10
    try {
      Future.join {
        (1 to count).map {
          i => Futures {
            invoked.add(i)
            throw new RuntimeException()
          }
        }
      }.get()
    } catch {
      case throwable: Throwable => //
    }
    invoked.size() should be(count)
  }
}
