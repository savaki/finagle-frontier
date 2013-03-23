package com.github.savaki.finagle.proxy

import org.scalatest.FlatSpec
import com.twitter.finagle.http.{Http, Request}
import com.twitter.finagle.Service
import com.twitter.util.Future
import java.net.InetSocketAddress
import org.jboss.netty.handler.codec.http._
import org.scalatest.matchers.ShouldMatchers
import com.twitter.finagle.builder.ServerBuilder
import com.twitter.conversions.time._

/**
 * @author matt.ho@gmail.com
 */
class ProxyTest extends FlatSpec with ShouldMatchers {
  def newRequest(originalHost: String = "192.168.1.1", originalPort: Int = 8000): Request = {
    def httpRequest = new DefaultHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, "/")
    val remoteSocketAddr = new InetSocketAddress(originalHost, originalPort)
    ProxyRequest(remoteSocketAddr, httpRequest)
  }

  "#apply" should "update x-remote-addr with remoteHost" in {
    val remoteHost: String = "192.168.1.1"

    val service = new Service[HttpRequest, String] {
      def apply(request: HttpRequest): Future[String] = Future.value(request.getHeader("x-remote-addr"))
    }
    val proxy = new Proxy(service)
    val request = newRequest(remoteHost)

    proxy(request).get() should be(remoteHost)
  }

  "#foo" should "proxy loyal3 site" in {
    val proxy: Proxy[HttpResponse] = new Proxy(new ServiceRegistry())
    val server = ServerBuilder() // 4
      .name("HelloService")
      .requestTimeout(10.seconds)
      .bindTo(new InetSocketAddress(8080))
      .codec(Http())
      .build(proxy)

    server.toString
    Thread.sleep(500000)
  }
}
