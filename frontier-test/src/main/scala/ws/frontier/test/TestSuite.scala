package ws.frontier.test

import org.scalatest.matchers.ShouldMatchers
import org.scalatest.FlatSpec

/**
 * @author matt
 */

abstract class TestSuite extends FlatSpec with ShouldMatchers {
  def time(count: Int = 1000, timeout: Long = 10000)(function: => Any) {
    val started = System.currentTimeMillis()
    var i = 0
    while (i < count) {
      function
      i = i + 1
    }
    val elapsed = System.currentTimeMillis() - started
    elapsed should be < timeout
    println("%s iterations completed in %sms (%06.5f ms/per)" format(count, elapsed, elapsed.toDouble / count.toDouble))
  }
}
