package ws.frontier.integration

import com.twitter.finagle.http.{Response, Request}
import org.jboss.netty.handler.codec.http.HttpMethod

/**
 * @author matt.ho@gmail.com
 */
class PostTest extends IntegrationSuite {
  "POST" should "pass through query parameters" in {
    withLocalService {
      client => {
        val request = Request(HttpMethod.POST, "/?hello=world&a=b")
        val response: Response = client(request).get()
        val contents: Array[String] = response.getContentString().split("\n")
        contents should contain("param.hello=world")
        contents should contain("param.a=b")
      }
    }
  }

  it should "also handle passed through parameters" in {
    withLocalService {
      client => {
        val request = Request("/")
        request.setContentString("a=b")
        request.setMethod(HttpMethod.POST)

        val response: Response = client(request).get()
        val contents: Array[String] = response.getContentString().split("\n")
        contents should contain("body=a=b")
      }
    }
  }

  it should "handle query parameters and passed parameters at the same time" in {
    withLocalService {
      client => {
        val request = Request("/?hello=world")
        request.setMethod(HttpMethod.POST)
        request.setContentString("a=b")

        val response: Response = client(request).get()
        val contents: Array[String] = response.getContentString().split("\n")
        contents should contain("param.hello=world")
        contents should contain("body=a=b")
        contents should contain("uri=/?hello=world")
      }
    }
  }
}
