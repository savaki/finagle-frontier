package ws.frontier.core.converter

import java.io.{InputStream, FileInputStream, File}
import java.net.URL
import java.util
import scala.collection.JavaConversions._
import util.Properties
import ws.frontier.core.{HttpProxyTrail, AggregatingTrail}

/**
 * @author matt.ho@gmail.com
 */
object PluginRegistry {
  val defaultPlugins: Map[String, String] = Map(
    "default-list" -> classOf[AggregatingTrail[_, _]].getCanonicalName,
    "default" -> classOf[HttpProxyTrail].getCanonicalName
  )

  private[this] val registry = new ThreadLocal[Map[String, String]] {
    override def initialValue() = defaultPlugins
  }

  def withPlugins[T](plugins: Map[String, String] = defaultPlugins)(function: => T): T = {
    val original: Map[String, String] = registry.get()
    registry.set(plugins)
    try {
      function
    } finally {
      registry.set(original)
    }
  }

  def currentPlugins: Map[String, String] = registry.get()
}

