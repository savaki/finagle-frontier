package ws.frontier.core

import com.fasterxml.jackson.annotation.JsonProperty
import com.github.jknack.handlebars.{Template, Handlebars}
import com.twitter.finagle.http.{Response, Request}
import com.twitter.finagle.{Service, Filter}
import com.twitter.util.Future
import java.util.HashMap
import scala.beans.BeanProperty
import java.util.regex.Pattern
import ws.frontier.core.util.Banner

/**
 * @author matt.ho@gmail.com
 */
class Decorator extends Filter[Request, Response, Request, Response] {
  @BeanProperty
  var name: String = null

  @JsonProperty("content_type")
  @BeanProperty
  var contentType: String = "text/html"

  @BeanProperty
  var uri: String = null

  @BeanProperty
  var context: Array[ContextEntryBuilder] = null

  @BeanProperty
  var exclude: Array[String] = null

  private[this] var excludePatterns: Array[Pattern] = null

  private[core] var template: Template = null

  private[core] var territory: Territory[Request, Response] = null

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

  def apply(request: Request, service: Service[Request, Response]): Future[Response] = {
    val uriContexts: Future[Seq[(String, String)]] = getUriContexts(request)

    service(request).map {
      response => {
        if (contentType != null && contentType.equalsIgnoreCase(response.getHeader(Decorator.CONTENT_TYPE))) {
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
        uriContexts.map(_.apply(territory))
      }
    } else {
      Future.value(Seq())
    }
  }

  def isExcluded(request: Request): Boolean = {
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
    val html: String = template(requestContext)
    val bytes: Array[Byte] = html.getBytes("UTF-8")
    response.removeHeader("Content-Length")
    response.addHeader("Content-Length", bytes.length.toString)
    response.setContentString(html)
    response
  }

  def getTemplateSource(territory: Territory[Request, Response]): Future[String] = {
    val request = Request(uri)
    val future: Future[Response] = territory.getTrail.apply(request).getOrElse {
      throw new RuntimeException("unable to load template, %s, from trails" format uri)
    }

    future.map {
      response => response.getContentString()
    }
  }

  def compileTemplate(hbs: String): Template = {
    Decorator.handlebars.compile(hbs)
  }

  def initialize(territory: Territory[Request, Response]): Future[Unit] = {
    excludePatterns = Option(exclude)
      .getOrElse(Array[String]())
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

    getTemplateSource(territory).map {
      hbs => template = compileTemplate(hbs)
    }
  }
}

object Decorator {
  val CONTENT_TYPE = "Content-Type"

  private[core] val handlebars: Handlebars = new Handlebars()
}


