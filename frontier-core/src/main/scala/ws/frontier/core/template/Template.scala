package ws.frontier.core.template

import java.util.{Map => JMap}

/**
 * @author matt
 */

trait Template {
  def apply(context: JMap[String, String]): String
}

object Template {
  /**
   * the name of the default context parameter that contains our dynamic content
   */
  val CONTENT = "content"
}

trait TemplateFactory {
  /**
   * @return the unique name of this template factory e.g. default, velocity
   */
  def name: String

  def compile(text: String): Template
}

class PassThroughTemplateFactory extends TemplateFactory {

  /**
   * @return the unique name of this template factory e.g. default, velocity
   */
  def name = "default"

  def compile(text: String): Template = new PassThroughTemplate
}

class PassThroughTemplate extends Template {
  def apply(context: JMap[String, String]): String = {
    context.get("content")
  }
}
