package ws.frontier.core

import ws.frontier.test.TestSuite
import com.twitter.util.Future
import ws.frontier.core.AggregatingTrail

/**
 * @author matt
 */

class AggregatingTrailTest extends TestSuite {
  val good = new EchoTrail[String] {
    override def apply(request: String) = Some(Future.value(request))
  }

  val exceptional = new EchoTrail[String] {
    override def apply(request: String) = throw new RuntimeException
  }

  "#apply" should "continue trying trails until a match is found (e.g. Some returned)" in {
    var noMatchCalled = false
    val noMatch = new EchoTrail[String] {
      override def apply(request: String) = {
        noMatchCalled = true
        None
      }
    }

    val expected: String = "hello world"
    val aggregator = new AggregatingTrail(noMatch, good)
    val result: Option[Future[String]] = aggregator(expected)
    result.isDefined should be(true)
    result.get.get() should be(expected)
    noMatchCalled should be(true) // we expected noMatch to be invoked first
  }

  it should "not continue once a match has been found" in {
    val aggregator = new AggregatingTrail(good, exceptional)
    aggregator("hello world").isDefined should be(true)
  }
}