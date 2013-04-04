package ws.frontier.core.converter

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.module.SimpleModule
import java.io.File
import java.net.URL
import ws.frontier.core.Trail

/**
 * @author matt
 */

class FrontierMapper {
  private[converter] val trail: TrailConverter = new TrailConverter

  private[this] val mapper = {
    val m = new ObjectMapper

    val module = new SimpleModule
    module.addDeserializer(classOf[Trail[_, _]], trail)
    m.registerModule(module)

    m
  }

  def readValue[T](json: String)(implicit t: Manifest[T]): T = {
    mapper.readValue(json, t.erasure.asInstanceOf[Class[T]])
  }

  def readValue[T](file: File)(implicit t: Manifest[T]): T = {
    mapper.readValue[T](file, t.erasure.asInstanceOf[Class[T]])
  }

  def readValue[T](url: URL)(implicit t: Manifest[T]): T = {
    mapper.readValue[T](url, t.erasure.asInstanceOf[Class[T]])
  }
}

object FrontierMapper extends FrontierMapper
