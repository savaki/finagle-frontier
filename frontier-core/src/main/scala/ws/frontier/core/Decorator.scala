package ws.frontier.core

import com.fasterxml.jackson.annotation.JsonProperty
import com.github.jknack.handlebars.{Template, Handlebars}
import com.twitter.finagle.http.{Response, Request}
import com.twitter.finagle.{Service, Filter}
import com.twitter.util.Future
import java.util.HashMap
import scala.beans.BeanProperty
import java.util.regex.Pattern
import ws.frontier.core.util.{Logging, Banner}

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

  private[core] var uriContexts: Array[UriContext] = {
    Option(context)
      .getOrElse(Array[ContextEntryBuilder]())
      .map(_.buildUriContext())
      .filter(_ != None)
      .map(_.get)
  }

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
    if( request.getHeader(Decorator.DECORATOR_HEADER) != null ) {
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
    val html: String = template.get(requestContext)
    val bytes: Array[Byte] = html.getBytes("UTF-8")
    response.removeHeader("Content-Length")
    response.addHeader("Content-Length", bytes.length.toString)
    response.setContentString(html)
    response
  }

  def getTemplateSource(trail: Trail[Request, Response]): Future[String] = {
    val request = Request(uri)
    val future: Future[Response] = trail(request).getOrElse {
      throw new RuntimeException("unable to load template, %s, from trails" format uri)
    }

    future.map {
      response => response.getContentString()
    }
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

    trail = registry.trail(trailId).getOrElse {
      throw new RuntimeException("unable to load template!  no trail found with id, %s" format trailId)
    }.asInstanceOf[Trail[Request, Response]]

    getTemplateSource(trail).map {
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

  private[core] val handlebars: Handlebars = new Handlebars()
}


