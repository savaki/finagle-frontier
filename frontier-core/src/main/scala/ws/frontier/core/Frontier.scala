package ws.frontier.core

import config.{FrontierConfig, FrontierMapper}
import java.net.{InetSocketAddress, URL}
import com.twitter.finagle.builder.{ServerBuilder, ClientBuilder}
import com.twitter.conversions.time._
import com.twitter.finagle.http.{RichHttp, Http, Response, Request}
import com.twitter.finagle.Service
import com.twitter.util.Future

/**
 * @author matt
 */

class Frontier {
  def start() {
    val resource: URL = getClass.getClassLoader.getResource("frontier.json")
    val config: FrontierConfig = FrontierMapper.readValue(resource)

    config.validate()
  }
}

class SampleProxy(csop: Service[Request, Response], web: Service[Request, Response]) extends Service[Request, Response] {
  def handle(request: Request, service: Service[Request, Response], host: String): Future[Response] = {
    request.removeHeader("host")
    request.addHeader("host", host)
    service(request)
  }

  def apply(request: Request): Future[Response] = {
    request.getUri match {
      case uri if uri.startsWith("/login") => handle(request, csop, "csop.loyal3.com:443")
      case uri if uri.startsWith("/signup") => handle(request, csop, "csop.loyal3.com:443")
      case _ => handle(request, web, "www.loyal3.com:80")
    }
  }
}

object Frontier {
  def main(args: Array[String]) {
    println("a")
    val sample = Future.value("hello world")
    println("b")

    val csop = ClientBuilder()
      .connectTimeout(10.seconds)
      .codec(RichHttp[Request](Http()))
      .hosts("csop.loyal3.com:443")
      .tls("csop.loyal3.com")
      .hostConnectionLimit(1024)
      .build()

    val web = ClientBuilder()
      .connectTimeout(10.seconds)
      .codec(RichHttp[Request](Http()))
      .hosts("www.loyal3.com:80")
      .hostConnectionLimit(1024)
      .build()

    val server = ServerBuilder()
      .name("FrontierServer")
      .codec(RichHttp[Request](Http()))
      .bindTo(new InetSocketAddress(9080))
      .build(new SampleProxy(web, csop))
  }

}