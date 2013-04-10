package ws.frontier.core

import com.twitter.finagle.http.{RichHttp, Http, Response, Request}
import com.twitter.util.Future
import beans.BeanProperty
import com.twitter.finagle.Service
import com.twitter.finagle.builder.ClientBuilder
import com.twitter.conversions.time._
import java.util.regex.Pattern
import com.fasterxml.jackson.annotation.JsonProperty

/**
 * @author matt.ho@gmail.com
 */
class HttpProxyTrail extends Trail[Request, Response] {
  /**
   * HttpProxyTrail provides the basic unit of work for HTTP based trails.
   *
   * Note: this currently doesn't support chunked or streaming calls
   *
   * @return None if this trail cannot handled the provided request; Some(Future[OUT]) if the action was handled
   */
  def apply(request: Request): Option[Future[Response]] = {
    if (matches(request)) {
      request.removeHeader("host")
      request.addHeader("host", host)
      Some(service(request))
    } else {
      None
    }
  }

  def matches(request: Request): Boolean = {
    if (matchers == null || matchers.length == 0) {
      // no matchers defined?  then accept anything
      true

    } else {
      // otherwise, only return true if we find a match
      var index = 0
      val uri = request.uri
      while (index < matchers.length) {
        if (matchers(index).matcher(uri).matches()) {
          return true
        }
        index = index + 1
      }
      false
    }
  }

  /**
   * [REQUIRED] the array of hosts to connect to in the format hostname:port
   */
  @BeanProperty
  var hosts: Array[String] = null

  /**
   * [OPTIONAL] the value to use for the Host HTTP-Header.  if this value is not provided, hosts(0) will be used
   */
  @BeanProperty
  var host: String = null

  /**
   * [OPTIONAL] what url paths are covered by this trail.  a * wildcard is allowed.
   * <br/>
   * <br/>
   * Examples:
   *
   * <ul>
   * <li>/images/&#42;</li>
   * <li>*.gif</li>
   * <li>/a-very/specific/path.html</li>
   * </ul>
   */
  @BeanProperty
  var locations: Array[String] = null

  /**
   * [OPTIONAL] how long (in seconds) we should attempt a connection before timing out (defaults to 5 seconds)
   */
  @BeanProperty
  var timeout: Int = 5

  @JsonProperty("enable_tls")
  @BeanProperty
  var enableTLS: Boolean = false

  @BeanProperty
  var tcpConnectTimeout: Int = 5

  /**
   * [OPTIONAL] how many concurrent connections should we allow to the specified host (defaults to 1024)
   */
  @BeanProperty
  var hostConnectionLimit = 1024

  private[this] var service: Service[Request, Response] = null

  private[this] var matchers: Array[Pattern] = null

  /**
   * @return a future that allows us to mark when initialization is complete
   */
  override def initialize(): Future[Unit] = Future.value {
    // initialize the matchers
    matchers = if (locations != null) {
      locations.map {
        value =>
          var regex = value
          if (regex.startsWith("^") == false) regex = "^" + regex
          if (regex.endsWith("$") == false) regex = regex + "$"
          regex.replaceAllLiterally("*", ".*").r.pattern
      }
    } else {
      Array[Pattern]()
    }

    // assign this.host if it wasn't already set
    if (host == null) {
      if (hosts != null && hosts.length > 0) {
        host = hosts(0)
      } else {
        throw new RuntimeException("no hosts value provided!")
      }
    }
  }

  def start(): Future[Unit] = {
    Future.value {
      synchronized {
        if (service == null) {
          val builder = ClientBuilder()
            .codec(RichHttp[Request](Http()))
            .hosts(hosts.mkString(","))
            .timeout(timeout.seconds)
            .tcpConnectTimeout(tcpConnectTimeout.seconds)
            .hostConnectionLimit(hostConnectionLimit)

          if (enableTLS) {
            val tlsHosts: String = hosts.map(_.split(":").head).mkString(",") // strip off port number
            service = builder.tls(tlsHosts).build()

          } else {
            service = builder.build()
          }
        }
      }
    }
  }

  def shutdown(): Future[Unit] = {
    if (service != null) {
      service.close().onSuccess {
        unit => this.service = null
      }
    } else {
      Future()
    }
  }

  override def toString: String =
    """
      |HttpProxyTrail {
      | locations: %s
      | hosts:     %s
      |}
    """.stripMargin format(
      if (locations == null) "default" else locations.mkString(", "),
      if (hosts == null) "none" else hosts.mkString(", ")
      )
}

