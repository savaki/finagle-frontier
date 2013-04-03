package ws.frontier.core.config

import ws.frontier.core.Plugin
import ws.frontier.core.Kind
import ws.frontier.core.util.Logging

/**
 * @author matt
 */

case class PluginConfig(name: String, kind: Kind, klass: String, params: Map[String, String] = Map()) extends Validating with Logging {
  private[this] var errors: Array[ValidationError] = Array()

  private[this] val plugin: Option[Plugin[_, _]] = {
    try {
      val instance: Plugin[_, _] = Class.forName(klass).newInstance().asInstanceOf[Plugin[_, _]]
      Some(instance)

    } catch {
      case notFound: ClassNotFoundException =>
        val gripe: String = "unable to instantiate class, %s" format klass
        errors = Array(ValidationError(gripe))
        debug(gripe)
        None

      case throwable: Throwable => {
        val gripe: String = "unable to instantiate class, %s => %s" format(klass, throwable.getMessage)
        errors = Array(ValidationError(gripe))
        debug(gripe)
        None
      }
    }
  }

  def validate(): Array[ValidationError] = {
    plugin.map(_.validate(this) ++ errors).getOrElse {
      errors
    }
  }
}
