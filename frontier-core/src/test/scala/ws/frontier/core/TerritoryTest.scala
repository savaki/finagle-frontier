package ws.frontier.core

import converter.FrontierMapper
import ws.frontier.test.TestSuite
import com.twitter.finagle.http.{RichHttp, Http, Request, Response}
import com.twitter.finagle.builder.ClientBuilder
import com.twitter.conversions.time._

/**
 * @author matt.ho@gmail.com
 */
class TerritoryTest extends TestSuite {
  "#deserialize" should "deserialize territory with one trail" in {
    val json =
      """
        |{
        | "port": 8000,
        | "trail": [
        |   {
        |     "locations":["/bugs/*"]
        |   }
        | ]
        | }
        |}
      """.stripMargin
    val territory: Territory[Request, Response] = FrontierMapper.readValue[Territory[Request, Response]](json)
    territory should not(be(null))
    territory.port should be(8000)
    territory.trail should not(be(null))
    territory.trail.isInstanceOf[AggregatingTrail[Request, Response]] should be(true)

    val trails: Seq[Trail[Request, Response]] = territory.trail.asInstanceOf[AggregatingTrail[Request, Response]].trails
    trails.length should be(1)

    val proxy: HttpProxyTrail = trails.head.asInstanceOf[HttpProxyTrail]
    proxy.locations should be(Array("/bugs/*"))
  }

  "#start/#shutdown" should "start a territory" in {
    val json =
      """
        |{
        | "port": 8000,
        | "name": "home",
        | "trail": [
        |   {
        |     "hosts": ["www.github.com:80"]
        |   }
        | ]
        | }
        |}
      """.stripMargin
    val territory: Territory[Request, Response] = FrontierMapper.readValue[Territory[Request, Response]](json)
    val client = ClientBuilder()
      .codec(RichHttp[Request](Http()))
      .hosts("localhost:%s" format territory.port)
      .connectTimeout(10.seconds)
      .hostConnectionLimit(64)
      .tcpConnectTimeout(10.seconds)
      .build()

    val registry: Registry[Request, Response] = new EmptyRegistry[Request, Response]()

    territory.initialize(registry).get()
    territory.start(registry).get()
    val response: Response = client(Request("/")).get()
    response.statusCode should be(301)
    territory.shutdown().get()
  }
}
