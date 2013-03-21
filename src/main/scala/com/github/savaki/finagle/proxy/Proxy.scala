package com.github.savaki.finagle.proxy

import com.twitter.finagle.Service
import com.twitter.finagle.http.Request
import com.twitter.util.Future
import org.jboss.netty.handler.codec.http.HttpRequest
import java.net.InetSocketAddress
import scala.collection.JavaConversions._

/**
 * @author matt.ho@gmail.com
 */
class Proxy[OUT](var service: Service[HttpRequest, OUT]) extends Service[HttpRequest, OUT] {
  def apply(request: HttpRequest): Future[OUT] = {
    request.setHeader("host", "www.loyal3.com:80")
    println("------------------------------------------------------------")
    request.getHeaderNames.map {
      name => println("%20s => %s" format(name, request.getHeader(name)))
    }
    //    requestCopy.setHeader("x-remote-addr", original.remoteHost)

    service(request)
  }
}

case class ProxyRequest(remoteSocketAddress: InetSocketAddress, httpRequest: HttpRequest) extends Request {
}


