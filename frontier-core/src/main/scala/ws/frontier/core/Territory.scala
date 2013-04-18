package ws.frontier.core

import beans.BeanProperty
import com.twitter.finagle.Service
import com.twitter.finagle.builder.{Server, ServerBuilder}
import com.twitter.finagle.http.RichHttp
import com.twitter.finagle.http.{Response, Http, Request}
import com.twitter.util.Future
import java.net.InetSocketAddress

/**
 * @author matt.ho@gmail.com
 */
class Territory[IN, OUT] {
  @BeanProperty
  var port: Int = 9080

  @BeanProperty
  var name: String = null

  @BeanProperty
  var trail: Trail[IN, OUT] = null

  private[this] var server: Server = null

  def initialize(): Future[Unit] = trail.initialize()

  def banner(log: Banner) {
    log("Territory: {")
    log.child {
      if (name != null) {
        log(s"name: ${name}")
      }
      log(s"port: ${port}")
      trail.banner(log)
    }
    log("}")
  }

  def start(registry: Registry[IN, OUT]): Future[Int] = {
    trail.start(registry).map {
      unit =>
        val httpTrail: Trail[Request, Response] = trail.asInstanceOf[Trail[Request, Response]]
        server = ServerBuilder()
          .name("Frontier-%s" format port)
          .codec(RichHttp[Request](Http()))
          .bindTo(new InetSocketAddress(port))
          .build(new TrailService[Request, Response](httpTrail))
        port
    }
  }

  def shutdown(): Future[Unit] = {
    trail.shutdown().map {
      unit => server.close()
    }.map {
      unit => server = null
    }
  }
}

class TrailService[IN, OUT](trail: Trail[IN, OUT]) extends Service[IN, OUT] {
  def apply(request: IN): Future[OUT] = {
    trail(request).get
  }
}
