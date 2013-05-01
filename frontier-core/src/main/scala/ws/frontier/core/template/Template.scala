package ws.frontier.core.template

import java.util.{Map => JMap}
import scala.collection.JavaConversions._
import scala.collection.JavaConverters._

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

  def compile(text: String): Template = new PassThroughTemplate(text)
}

class PassThroughTemplate(template: String) extends Template {
  def apply(context: JMap[String, String]): String = {
    var result = template
    context.foreach {
      case (key, value) => {
        val literal = "${%s}".format(key)
        result = result.replaceAllLiterally(literal, value)
      }
    }
    result
  }
}
