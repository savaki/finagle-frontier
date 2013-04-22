package ws.frontier.template.velocity

import java.io.StringWriter
import java.util.{Map => JMap}
import org.apache.velocity.runtime.parser.node.SimpleNode
import org.apache.velocity.runtime.{RuntimeServices, RuntimeSingleton}
import org.apache.velocity.{Template => VTemplate, VelocityContext}
import ws.frontier.core.template.{TemplateFactory, Template}

/**
 * @author matt
 */

case class VelocityTemplate(underlying: VTemplate) extends Template {
  def apply(context: JMap[String, String]): String = {
    val writer = new StringWriter(4096)
    underlying.merge(new VelocityContext(context), writer)
    writer.toString
  }
}

class VelocityTemplateFactory extends TemplateFactory {
  private[this] val runtimeServices: RuntimeServices = RuntimeSingleton.getRuntimeServices

  /**
   * @return the unique name of this template factory e.g. pass-through, velocity
   */
  def name = "velocity"

  def compile(text: String): Template = {
    val data: SimpleNode = runtimeServices.parse(text, text)
    val underlying = new VTemplate
    underlying.setRuntimeServices(runtimeServices)
    underlying.setData(data)
    underlying.initDocument()
    VelocityTemplate(underlying)
  }
}
