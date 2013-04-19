package ws.frontier.core

import com.twitter.finagle.Service
import com.twitter.finagle.http.{Response, Request}
import com.twitter.util.Future
import java.io.File
import org.jboss.netty.handler.codec.http.{HttpResponseStatus, HttpVersion}
import ws.frontier.core.converter.FrontierMapper
import ws.frontier.test.TestSuite

/**
 * @author matt.ho@gmail.com
 */
class DecoratorTest extends TestSuite {
  val decorator = {
    val instance = new Decorator() {
      override def fetchTemplateSource(): Future[String] = {
        Future.value( """<div>{{content}}</div>""")
      }
    }
    instance.initialize(new EmptyRegistry[Request, Response])
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

  "mapper" should "parse decorator with ContextEntry" in {
    val json =
      """
        |{
        |  "content_type": "text/html",
        |  "uri": "/sample/default.html",
        |  "context": [
        |    {
        |      "name": "session",
        |      "uri": "/session/header",
        |      "timeout": 50
        |    },
        |    {
        |      "name": "title",
        |      "header": "X-Title"
        |    }
        |  ]
        |}
      """.stripMargin
    val decorator: Decorator = FrontierMapper.readValue[Decorator](json)
    decorator should not(be(null))
    decorator.context.length should be(2)
    decorator.context.head.header should be(null)
    decorator.context.head.uri should be("/session/header")
    decorator.context.head.timeout should be(50)
    decorator.context.last.header should be("X-Title")
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

  "#fetchTemplateFromURL" should "read from file url" in {
    val decorator = new Decorator
    decorator.uri = new File("frontier-core/src/test/resources/fetchTemplateFromURL.txt").toURI.toString
    decorator.fetchTemplateFromURI().get should include("valid")
  }
}
