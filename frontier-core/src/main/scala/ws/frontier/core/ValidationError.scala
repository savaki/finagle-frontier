package ws.frontier.core

/**
 * @author matt
 */

case class ValidationError(field: String, code: String, message: String = "")
