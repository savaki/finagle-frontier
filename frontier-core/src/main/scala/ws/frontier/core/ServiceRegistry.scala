package ws.frontier.core

import com.twitter.finagle.http.Http
import com.twitter.util.Future
import com.twitter.finagle.Service
import com.twitter.finagle.builder.ClientBuilder
import com.twitter.conversions.time._
import org.jboss.netty.handler.codec.http.{HttpRequest, HttpResponse}

/**
 * @author matt.ho@gmail.com
 */
class ServiceRegistry extends Service[HttpRequest, HttpResponse] {
  val underlying = ClientBuilder()
    .codec(Http())
    .hosts("www.loyal3.com:80")
    .hostConnectionLimit(5)
    .tcpConnectTimeout(10.seconds)
    .timeout(10.seconds)
    .build()

  def apply(request: HttpRequest): Future[HttpResponse] = {
    underlying(request)
  }
}
