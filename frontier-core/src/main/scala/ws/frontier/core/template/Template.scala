package ws.frontier.core.template

import java.util

/**
 * @author matt
 */

trait Template {
  def apply(context: util.HashMap[String, String]): String
}

object Template {
  /**
   * the name of the default context parameter that contains our dynamic content
   */
  val CONTENT = "content"
}

trait TemplateFactory {
  def compile(text: String): Template
}

class PassThroughTemplateFactory extends TemplateFactory {
  def compile(text: String): Template = new PassThroughTemplate
}

class PassThroughTemplate extends Template {
  def apply(context: util.HashMap[String, String]): String = {
    context.get("content")
  }
}
