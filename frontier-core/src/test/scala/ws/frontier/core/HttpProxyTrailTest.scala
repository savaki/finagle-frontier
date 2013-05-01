package ws.frontier.core

import com.twitter.finagle.http.{Response, Request}
import com.twitter.util.Future
import scala.collection.JavaConverters._
import ws.frontier.test.TestSuite

/**
 * @author matt.ho@gmail.com
 */
class HttpProxyTrailTest extends TestSuite {
  val trail = {
    val t = new HttpProxyTrail
    t.locations = Array("/blog/*", "*.gif")
    t.host = "localhost"
    t.initialize()
    t
  }

  "#apply" should "return None if no match" in {
    val request = Request("/something-else")
    trail(request).isDefined should be(false)
  }

  "#matches" should "match patterns with *" in {
    trail.matches(Request("/something-else")) should be(false)
    trail.matches(Request("/blog")) should be(false)
    trail.matches(Request("/blog/")) should be(true)
    trail.matches(Request("/blog/some_page.html")) should be(true)
    trail.matches(Request("/images/sample.gif")) should be(true)
  }

  "HttpProxyTrail" should "start up and respond to queries" in {
    val trail = new HttpProxyTrail
    trail.hosts = Array("www.google.com:80")
    trail.initialize()
    trail.start(new EmptyRegistry[Request, Response]).get()

    val result: Option[Future[Response]] = trail(Request("/"))
    result.isDefined should be(true)

    val response: Response = result.get.get
    response.getHeaderNames().asScala.foreach {
      name => println("%15s: %s" format(name, response.getHeader(name)))
    }
    response.getStatusCode() should be(200)

    trail.shutdown().get()
  }
}
