package ws.frontier.core

import com.fasterxml.jackson.annotation.JsonProperty
import com.github.jknack.handlebars.{ParserFactory, Template, Handlebars}
import com.twitter.finagle.http.{Response, Request}
import com.twitter.finagle.{Service, Filter}
import com.twitter.util.Future
import java.util.HashMap
import scala.beans.BeanProperty
import java.util.regex.Pattern
import ws.frontier.core.util.{Logging, Banner}
import java.net.{URI, URLConnection, URL}
import java.io.InputStream
import scala.io.Source
import com.github.jknack.handlebars.Handlebars.SafeString
import scala.collection.JavaConversions._

/**
 * @author matt.ho@gmail.com
 */
class Decorator extends Filter[Request, Response, Request, Response] with Logging {
  @BeanProperty
  var name: String = null

  @JsonProperty("content_type")
  @BeanProperty
  var contentType: String = "text/html"

  @BeanProperty
  var uri: String = null

  @JsonProperty("trail_id")
  @BeanProperty
  var trailId: String = null

  @BeanProperty
  var context: Array[ContextEntryBuilder] = null

  @BeanProperty
  var exclude: Array[String] = null

  private[this] var excludePatterns: Array[Pattern] = null

  private[core] var template: Option[Template] = None

  private[core] var trail: Trail[Request, Response] = null

  private[core] var headerContexts: Array[HeaderContext] = null

  private[core] var uriContexts: Array[UriContext] = null

  def banner(log: Banner) {
    log()
    log("Decorator {")
    log.child {
      log("content_type: %s" format contentType)
      log("uri:          %s" format uri)
    }
    log("}")
  }

  protected[core] def isDecorated(request: Request, response: Response): Boolean = {
    var result = true

    if (result && template.isDefined == false) {
      debug("no template defined, skipping decorator step")
      result = false
    }

    val actualContentType: String = response.getHeader(Decorator.CONTENT_TYPE)
    if (result && (contentType == null || contentType.equalsIgnoreCase(actualContentType)) == false) {
      debug("content-type doesn't match defined content type")
      result = false
    }

    if (result && request.getHeader(Decorator.DECORATOR_HEADER) != null) {
      debug("decorator requests are not further decorated")
      result = false
    }

    result
  }

  def apply(request: Request, service: Service[Request, Response]): Future[Response] = {
    val uriContexts: Future[Seq[(String, String)]] = getUriContexts(request)

    service(request).map {
      response => {
        if (isDecorated(request, response)) {
          val headerContexts: Seq[(String, String)] = getHeaderContexts(response)
          val requestContext = newContext(uriContexts.get() ++ headerContexts)
          merge(response, requestContext)

        } else {
          response
        }
      }
    }
  }

  def newContext(values: Seq[(String, String)]): HashMap[String, String] = {
    val result = new HashMap[String, String]()
    values.foreach {
      entry => result.put(entry._1, entry._2)
    }
    result
  }

  def getHeaderContexts(response: Response): Seq[(String, String)] = {
    if (headerContexts == null || headerContexts.length == 0) {
      Seq()
    } else {
      headerContexts.map(_.apply(response))
    }
  }

  def getUriContexts(request: Request): Future[Seq[(String, String)]] = {
    if (uriContexts.length > 0 && !isExcluded(request)) {
      Future.collect {
        uriContexts.map(_.apply(trail))
      }
    } else {
      Future.value(Seq())
    }
  }

  def isExcluded(request: Request): Boolean = {
    if (request.getHeader(Decorator.DECORATOR_HEADER) != null) {
      return true
    }

    val uri = request.uri
    var index = 0
    while (index < excludePatterns.length) {
      if (excludePatterns(index).matcher(uri).matches()) {
        return true
      }
      index = index + 1
    }
    false
  }

  def merge(response: Response, requestContext: HashMap[String, String]): Response = {
    requestContext.put("content", response.getContentString())

    val safeContext: HashMap[String, SafeString] = new HashMap[String, SafeString]()
    requestContext.foreach {
      pair => safeContext.put(pair._1, new SafeString(pair._2))
    }

    val html: String = template.get(safeContext)
    val bytes: Array[Byte] = html.getBytes("UTF-8")
    response.removeHeader("Content-Length")
    response.addHeader("Content-Length", bytes.length.toString)
    response.setContentString(html)
    response
  }

  protected[core] def fetchTemplateFromTrail(): Future[String] = {
    val request = Request(uri)
    val future: Future[Response] = trail(request).getOrElse {
      throw new RuntimeException("unable to load template, %s, from trails" format uri)
    }

    future.map {
      response => response.getContentString()
    }
  }

  protected[core] def fetchTemplateFromURI(): Future[String] = {
    Future.value {
      val connection: URLConnection = new URI(uri).toURL.openConnection()
      val inputStream: InputStream = connection.getInputStream
      try {
        Source.fromInputStream(inputStream).mkString

      } finally {
        if (inputStream != null) {
          inputStream.close()
        }
      }
    }
  }

  def fetchTemplateSource(): Future[String] = {
    require(uri != null, "unable to fetch template from a null URL!")

    val result: Future[String] = uri match {
      case uri if uri.startsWith("/") => fetchTemplateFromTrail()
      case http if uri.startsWith("http://") => fetchTemplateFromURI()
      case file if uri.startsWith("file:") => fetchTemplateFromURI()
      case _ => throw new UnsupportedOperationException("unable to fetch template from %s!  %s is an unsupported protocol." format(uri, uri))
    }

    result.onSuccess {
      hbs => trace(hbs)
    }

    result
  }

  def compileTemplate(hbs: String): Template = {
    Decorator.handlebars.compile(hbs)
  }

  def initialize[IN, OUT](registry: Registry[IN, OUT]): Future[Unit] = {
    excludePatterns = Option(exclude)
      .getOrElse(Array[String]())
      .map(_.replaceAllLiterally("*", ".*"))
      .map(_.r.pattern)

    headerContexts = Option(context)
      .getOrElse(Array[ContextEntryBuilder]())
      .map(_.buildHeaderContext())
      .filter(_ != None)
      .map(_.get)

    uriContexts = Option(context)
      .getOrElse(Array[ContextEntryBuilder]())
      .map(_.buildUriContext())
      .filter(_ != None)
      .map(_.get)

    trail = registry.trail(trailId).get.asInstanceOf[Trail[Request, Response]]

    fetchTemplateSource.map {
      hbs => {
        warn("loaded template, %s, from trail [id: %s]" format(uri, trailId))
        template = Option(compileTemplate(hbs))
      }
    }
  }
}

object Decorator {
  val CONTENT_TYPE = "Content-Type"

  val DECORATOR_HEADER = "X-Decorator"

  private[core] val handlebars: Handlebars = {
    val compiler: Handlebars = new Handlebars()
    compiler
  }
}


