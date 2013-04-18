package ws.frontier.core.converter

import ws.frontier.test.TestSuite
import ws.frontier.core.Frontier
import com.twitter.finagle.http.{Request, Response}

/**
 * @author matt.ho@gmail.com
 */
class FrontierMapperTest extends TestSuite {
  "#readValue" should "set Frontier#trails when Trail#id set" in {
    val json =
      """
        |{
        |  "territories": [
        |    {
        |      "port": 9080,
        |      "trail": [
        |        {
        |          "id": "hello",
        |          "hosts": ["csop.loyal3.com:443"],
        |          "enable_tls": true,
        |          "tags": ["r192"]
        |        }
        |      ]
        |    }
        |  ]
        |}
      """.stripMargin

    val frontier = FrontierMapper.readValue[Frontier[Request, Response]](json)
    frontier.trails should not(be(null))
    frontier.trails("hello") should not(be(null))
  }
}
