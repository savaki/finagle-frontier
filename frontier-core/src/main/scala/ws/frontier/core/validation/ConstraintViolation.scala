package ws.frontier.core.validation

import com.twitter.finagle.Service
import org.jboss.netty.handler.codec.http.{HttpRequest, HttpResponse}
import com.twitter.util.Future

/**
 * @author matt
 */

class ConstraintViolation {

}

class SampleService extends Service[HttpRequest, HttpResponse] {
  def apply(request: HttpRequest): Future[HttpResponse] = {
    throw new UnsupportedOperationException
  }
}