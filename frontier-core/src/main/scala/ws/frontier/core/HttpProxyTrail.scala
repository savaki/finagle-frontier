package ws.frontier.core

import com.twitter.finagle.http.{RichHttp, Http, Response, Request}
import com.twitter.util.Future
import beans.BeanProperty
import com.twitter.finagle.{Filter, Service}
import com.twitter.finagle.builder.ClientBuilder
import com.twitter.conversions.time._
import java.util.regex.Pattern
import com.fasterxml.jackson.annotation.JsonProperty
import ws.frontier.core.util.Banner

/**
 * @author matt.ho@gmail.com
 */
class HttpProxyTrail extends Trail[Request, Response] with HttpCodecBuilder {
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

  @JsonProperty("enable_tls")
  @BeanProperty
  var enableTLS: Boolean = false

  /**
   * the list of decorators used to adorn this content.  each decorator will be applied as a filter in the order its
   * been defined in the json file
   */
  @BeanProperty
  var decorators: Array[String] = null

  private[this] var service: Service[Request, Response] = null

  private[this] var matchers: Array[Pattern] = null

  override def banner(log: Banner) {
    val locationsString = Option(locations).map(_.mkString(", ")).getOrElse("<any>")
    val message =
      s"""
        |ProxyTrail {
        |  hosts:                ${hosts.mkString(", ")}
        |  tls:                  ${if (enableTLS) "enabled" else "disabled"}
        |  locations:            ${locationsString}
        |}
      """.stripMargin
    log(message)
  }

  /**
   * HttpProxyTrail provides the basic unit of work for HTTP based trails.  The intent is for this to behave just like
   * a standard Finagle Service with one gotcha.  Rather than return Future[Response], this returns
   * Future[Option[Response]].  Doing this allows #apply to say "this request isn't for me, pass it to the next guy"
   *
   * Note: this currently doesn't support chunked or streaming calls
   *
   * @return None if this trail cannot handled the provided request; Some(Future[OUT]) if the action was handled
   */
  def apply(request: Request): Option[Future[Response]] = {
    if (matches(request)) {
      debug("%s %s", request.getMethod(), request.getUri())
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

  /**
   * construct a service that chains all the defined filters (like decorators) in front of the base service
   *
   * @param registry where filters can be found
   * @param baseService the base service to be wrapped
   * @return the wrapped with filters service
   */
  def applyFilters(registry: Registry[Request, Response], baseService: Service[Request, Response]): Service[Request, Response] = {
    val filterSeq: Seq[Filter[Request, Response, Request, Response]] = Option(decorators)
      .getOrElse(Array()).toSeq // ensure we have a valid Seq[String]
      .map(name => registry.decorator(name).get) // Seq[String] => Seq[Decorator]

    filterSeq.foldRight(baseService)((a, b) => a andThen b)
  }

  /**
   * constructs a fully configured service e.g. baseService with filters applied
   *
   * @param registry a storehouse for filters that can be searched by name
   * @return the fully configured service
   */
  protected def buildService(registry: Registry[Request, Response]): Service[Request, Response] = {
    val baseService: Service[Request, Response] = buildBaseService(registry.options)
    applyFilters(registry, baseService)
  }

  /**
   * @return a vanilla service devoid of any filters.
   */
  protected def buildBaseService(options: FrontierOptions): Service[Request, Response] = {
    val builder = ClientBuilder()
      .codec(RichHttp[Request](buildCodec(options)))
      .hosts(hosts.mkString(","))
      .timeout(options.timeout.seconds)
      .tcpConnectTimeout(options.tcpConnectTimeout.seconds)
      .hostConnectionLimit(options.hostConnectionLimit)

    if (enableTLS) {
      val tlsHosts: String = hosts.map(_.split(":").head).mkString(",") // strip off port number
      builder.tls(tlsHosts).build()

    } else {
      builder.build()
    }
  }

  def start(registry: Registry[Request, Response]): Future[Unit] = {
    Future.value {
      synchronized {
        if (service == null) {
          service = buildService(registry)
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

