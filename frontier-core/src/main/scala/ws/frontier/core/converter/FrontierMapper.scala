package ws.frontier.core.converter

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.module.SimpleModule
import java.io.File
import java.net.URL
import ws.frontier.core.{FrontierOptions, Frontier, Trail}

/**
 * @author matt
 */

class FrontierMapper {
  private[converter] val trail = new TrailConverter

  private[converter] val frontierOptions = new FrontierOptionsConverter

  private[this] val mapper = {
    val m = new ObjectMapper

    val module = new SimpleModule
    module.addDeserializer(classOf[Trail[_, _]], trail)
    module.addDeserializer(classOf[FrontierOptions], frontierOptions)
    m.registerModule(module)

    m
  }

  def readValue[T](json: String)(implicit t: Manifest[T]): T = {
    withTrails {
      mapper.readValue(json, t.erasure.asInstanceOf[Class[T]])
    }
  }

  def readValue[T](file: File)(implicit t: Manifest[T]): T = {
    withTrails {
      mapper.readValue[T](file, t.erasure.asInstanceOf[Class[T]])
    }
  }

  def readValue[T](url: URL)(implicit t: Manifest[T]): T = {
    withTrails {
      mapper.readValue[T](url, t.erasure.asInstanceOf[Class[T]])
    }
  }

  def withTrails[T](function: => T): T = {
    val (result, trails) = TrailConverter.captureTrails {
      function
    }

    if (result.isInstanceOf[Frontier[_, _]]) {
      result.asInstanceOf[Frontier[_, _]].withTrails(trails)
    }
    result
  }
}

object FrontierMapper extends FrontierMapper
