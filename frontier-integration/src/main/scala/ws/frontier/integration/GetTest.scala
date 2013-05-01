package ws.frontier.integration

import com.twitter.finagle.http.{Response, Request}


/**
 * @author matt.ho@gmail.com
 */
class GetTest extends IntegrationSuite {

  "GET" should "pass through query parameters" in {
    var finished = false

    withLocalService {
      client => {
        val request = Request("/?hello=world&a=b")
        val response: Response = client(request).get()
        val contents: Array[String] = response.getContentString().split("\n")
        contents should contain("param.hello=world")
        contents should contain("param.a=b")
        finished = true
      }
    }

    finished should be(true)
  }

  it should "pass arbitrary X headers" in {
    withLocalService {
      client => {
        val request = Request("/")
        request.setHeader("X-Foo", "bar")

        val response: Response = client(request).get()
        val contents: Array[String] = response.getContentString().split("\n")
        contents should contain("header.X-Foo=bar")
      }
    }
  }

}