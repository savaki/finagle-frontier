package ws.frontier.integration

import com.twitter.finagle.Service
import com.twitter.finagle.builder.{ClientBuilder, ServerBuilder}
import com.twitter.finagle.http.{Http, RichHttp, Response, Request}
import com.twitter.util.Future
import java.net.{ServerSocket, InetSocketAddress}
import org.jboss.netty.buffer.ChannelBuffers
import org.jboss.netty.handler.codec.http.HttpResponseStatus
import scala.collection.JavaConversions._
import ws.frontier.core.converter.FrontierMapper
import ws.frontier.core.{FrontierOptions, Frontier}
import ws.frontier.test.TestSuite

/**
 * @author matt.ho@gmail.com
 */
abstract class IntegrationSuite extends TestSuite {
  val echoService: Service[Request, Response] = new Service[Request, Response] {
    def apply(request: Request): Future[Response] = {
      def header(name: String) = request.getHeaders(name).mkString(",")
      def param(name: String) = request.getParams(name).mkString(",")

      val headers = request.getHeaderNames().map(name => s"header.${name}=${header(name)}").toList
      val params = request.getParamNames().map(name => s"param.${name}=${param(name)}").toList
      val other = List(
        s"uri=${request.uri}",
        s"path=${request.path}",
        s"method=${request.method}",
        s"body=${request.getContentString()}"
      )

      val contents: String = (headers ::: params ::: other).sorted.mkString("\n")
      val bytes = contents.getBytes
      val response: Response = Response(request.getProtocolVersion(), HttpResponseStatus.OK)
      response.setHeader("Content-Length", bytes.length.toString)
      response.setHeader("Content-Type", "text/plain")
      response.setContent(ChannelBuffers.wrappedBuffer(bytes))
      Future(response)
    }
  }

  def newEchoServer(port: Int, service: Service[Request, Response] = echoService) = {
    ServerBuilder()
      .name("GetTest")
      .codec(RichHttp[Request](Http()))
      .bindTo(new InetSocketAddress(port))
      .build(service)
  }

  def withLocalService[T](function: Service[Request, Response] => T): T = {
    val proxyPort = findAvailablePort()
    val underlyingPort = findAvailablePort()

    val underlying = newEchoServer(underlyingPort)
    val frontier = newFrontierInstance(proxyPort, underlyingPort)
    val client = newClient(proxyPort)

    frontier.initialize(FrontierOptions()).get()
    frontier.start().get()

    try {
      function(client)

    } finally {
      client.close().get()
      frontier.shutdown().get()
      underlying.close().get()
    }
  }

  def newClient(port: Int): Service[Request, Response] = {
    ClientBuilder()
      .codec(RichHttp[Request](Http()))
      .hostConnectionLimit(1024)
      .hosts(s"localhost:${port}")
      .build()
  }

  def newFrontierInstance[T](proxyPort: Int, underlyingPort: Int): Frontier[Request, Response] = {
    val configuration =
      s"""
        |{
        |  "territories": [
        |    {
        |      "port": ${proxyPort},
        |      "trail": [
        |        {
        |          "hosts": ["localhost:${underlyingPort}"],
        |          "tags": ["r194"]
        |        }
        |      ]
        |    }
        |  ]
        |}
      """.stripMargin
    FrontierMapper.readValue[Frontier[Request, Response]](configuration)
  }

  def findAvailablePort(): Int = {
    val socket = new ServerSocket(0)
    val port = socket.getLocalPort
    socket.close()
    port
  }
}
