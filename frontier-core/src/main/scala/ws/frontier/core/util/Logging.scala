package ws.frontier.core.util

import org.slf4j.LoggerFactory

/**
 * @author matt
 */

trait Logging {
  private[this] val logger = LoggerFactory.getLogger(this.getClass)

  def trace(message: String, args: AnyRef*) {
    logger.trace(message, args: _*)
  }

  def debug(message: String, args: AnyRef*) {
    logger.debug(message, args: _*)
  }

  def info(message: String, args: AnyRef*) {
    logger.info(message, args: _*)
  }

  def warn(message: String, throwable: Throwable = null) {
    logger.warn(message, throwable)
  }

  def error(message: String, args: AnyRef*) {
    logger.error(message, args: _*)
  }
}