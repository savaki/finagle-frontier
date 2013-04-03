package ws.frontier.core.config


/**
 * @author matt
 */

trait Validating {
  def validate(): Array[ValidationError]
}

case class ValidationError(message: String)



