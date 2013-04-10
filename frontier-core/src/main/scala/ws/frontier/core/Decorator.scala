package ws.frontier.core

import scala.beans.BeanProperty
import com.github.jknack.handlebars.{Template, Handlebars}
import com.twitter.util.Future
import com.twitter.finagle.http.{Response, Request}
import com.twitter.finagle.{Service, Filter}
import java.util.HashMap

/**
 * @author matt.ho@gmail.com
 */
class Decorator extends Filter[Request, Response, Request, Response] {
  @BeanProperty
  var contentType: String = "text/html"

  @BeanProperty
  var uri: String = null

  var template: Template = null

  def apply(request: Request, service: Service[Request, Response]): Future[Response] = {
    service(request).map {
      response => {
        if (contentType != null && contentType.equalsIgnoreCase(response.getHeader(Decorator.CONTENT_TYPE))) {
          merge(response)

        } else {
          response
        }
      }
    }
  }

  def merge(response: Response): Response = {
    val context = new HashMap[String, String]()
    context.put("content", response.getContentString())
    val html: String = template(context)
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
    getTemplateSource(territory).map {
      hbs => template = compileTemplate(hbs)
    }
  }
}

object Decorator {
  val CONTENT_TYPE = "Content-Type"

  private[core] val handlebars: Handlebars = new Handlebars()
}
