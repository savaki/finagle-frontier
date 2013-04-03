package ws.frontier.core.util

/**
 * @author matt
 */

trait Logging {
  def debug(message: String) {
    println(message)
  }

  def warn(message: String, throwable: Throwable = null) {
    println(message)
    if (throwable != null) {
      println(throwable.getStackTraceString)
    }
  }
}