package ws.frontier.core.util

import com.twitter.util.{Future, FuturePool}
import java.util.concurrent.{ThreadFactory, Executors}
import com.twitter.concurrent.NamedPoolThreadFactory

/**
 * @author matt.ho@gmail.com
 */
object Futures {
  private[this] val pool = FuturePool(Executors.newFixedThreadPool(4, new NamedPoolThreadFactory("frontier", true)))

  def apply[T](function: => T): Future[T] = {
    pool(function)
  }
}
