package ws.frontier.core

import converter.FrontierMapper
import ws.frontier.test.TestSuite
import com.twitter.finagle.http.{Request, Response}

/**
 * @author matt.ho@gmail.com
 */
class TerritoryTest extends TestSuite {
  "#deserialize" should "deserialize territory with one trail" in {
    val json =
      """
        |{
        | "port": 8000,
        | "trails": []
        |}
      """.stripMargin
    val territory: Territory[Request, Response] = FrontierMapper.readValue[Territory[Request, Response]](json)
    territory should not(be(null))
  }
}
