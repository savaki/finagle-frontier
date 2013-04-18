package ws.frontier.core

import com.twitter.conversions.time._
import com.twitter.finagle.http.{Response, Request}
import com.twitter.util.{Duration, Future}
import scala.beans.BeanProperty
import ws.frontier.core.util.Timer

/**
 * @author matt.ho@gmail.com
 */
class ContextEntryBuilder {
  @BeanProperty
  var name: String = null

  @BeanProperty
  var uri: String = null

  @BeanProperty
  var file: String = null

  @BeanProperty
  var timeout: Long = 100

  @BeanProperty
  var header: String = null

  def buildHeaderContext(): Option[HeaderContext] = {
    if (header != null) {
      Some(HeaderContext(name, header))
    } else {
      None
    }
  }

  def buildUriContext(): Option[UriContext] = {
    if (uri != null) {
      Some(UriContext(name, uri, timeout))
    } else {
      None
    }
  }
}

case class HeaderContext(name: String, header: String) {
  def apply(response: Response): (String, String) = {
    val value: String = response.getHeader(header)
    if (value == null) {
      name -> ""
    } else {
      name -> value
    }
  }
}

case class UriContext(name: String, uri: String, timeout: Long) {
  private[this] val duration: Duration = timeout.milliseconds

  def apply(trail: Trail[Request, Response]): Future[(String, String)] = {
    val request = Request(uri)
    request.addHeader(Decorator.DECORATOR_HEADER, "true") // marker to indicate that this is a decoration request

    trail.apply(request).get.within(Timer, duration).map {
      response => name -> response.getContentString()
    }.handle {
      case anythingElse => name -> ""
    }
  }
}
