package ws.frontier.core

import com.twitter.util.Future

/**
 * EchoTrail is a simple trail that echoes back what's sent to it.  Suitable for testing.
 *
 * @author matt.ho@gmail.com
 */
class EchoTrail[T] extends Trail[T, T] {
  /**
   * @return the value requested if it's not null; None otherwise
   */
  def apply(request: T): Option[Future[T]] = {
    if (request == null) {
      None
    } else {
      Option(Future.value(request))
    }
  }

  def start(registry: Registry[T, T]): Future[Unit] = Future()

  def shutdown(): Future[Unit] = Future()
}
