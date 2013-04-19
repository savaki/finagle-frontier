package ws.frontier.template.velocity

import ws.frontier.test.TestSuite
import ws.frontier.core.template.Template
import scala.collection.JavaConverters._

/**
 * @author matt
 */

class VelocityTemplateFactoryTest extends TestSuite {
  "#compile" should "create template from String" in {
    val key: String = "hello"
    val value: String = "world"

    val text =
      """
        |${key} = ${value}
      """.stripMargin
    val factory = new VelocityTemplateFactory
    val template: Template = factory.compile(text)
    val context = Map("key" -> key, "value" -> value).asJava
    val html: String = template(context)
    html should include("%s = %s".format(key, value))
  }
}
