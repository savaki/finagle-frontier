package ws.frontier.core

import ws.frontier.test.TestSuite
import com.twitter.finagle.http.{Response, Request}
import com.twitter.util.Future
import com.twitter.finagle.Service
import org.jboss.netty.handler.codec.http.{HttpResponseStatus, HttpVersion}

/**
 * @author matt.ho@gmail.com
 */
class DecoratorTest extends TestSuite {
  val decorator = {
    val instance = new Decorator() {
      override def getTemplateSource(territory: Territory[Request, Response]): Future[String] = {
        Future.value( """<div>{{content}}</div>""")
      }
    }
    instance.initialize(null)
    instance
  }

  val echoService = new Service[Request, Response] {
    def apply(request: Request): Future[Response] = {
      val response: Response = Response(HttpVersion.HTTP_1_1, HttpResponseStatus.OK)
      response.setContentString(request.getContentString())
      Future.value(response)
    }
  }


  val htmlService = new Service[Request, Response] {
    def apply(request: Request): Future[Response] = {
      echoService(request).map {
        response =>
          response.setHeader(Decorator.CONTENT_TYPE, decorator.contentType)
          response
      }
    }
  }

  "#apply" should "apply template for text/html" in {
    val request = Request("/")
    val content: String = "the time has come the walrus said"
    request.setContentString(content)
    decorator(request, htmlService).get().getContentString() should be("<div>%s</div>" format content)
  }

  it should "not render for non-text/html content" in {
    val request = Request("/")
    val content: String = "the time has come the walrus said"
    request.setContentString(content)
    decorator(request, echoService).get().getContentString() should be(content)
  }

  it should "be performant" in {
    val request = Request("/")
    val content: String = "the time has come the walrus said"
    request.setContentString(content)

    time(10000) {
      request.setContentString(content)
    }
  }
}
