package ws.frontier.core.config.converters

import com.fasterxml.jackson.databind.JsonNode
import scala.collection.JavaConversions._

/**
 * @author matt
 */

trait Deserializer[T] {

  protected def readValue(node: JsonNode): T

  private def readText(node: JsonNode): T = {
    throw new UnsupportedOperationException("#readText is not yet supported")
  }

  private def readArray(node: JsonNode)(implicit t: Manifest[T]): Array[T] = {
    node.iterator().flatMap(readNode(_)).toArray
  }

  def readNode(node: JsonNode)(implicit t: Manifest[T]): Array[T] = {
    if (node == null) {
      Array[T]()

    } else if (node.isObject) {
      Array(readValue(node))

    } else if (node.isArray) {
      readArray(node)

    } else if (node.isTextual) {
      Array(readText(node))

    } else {
      throw new RuntimeException("don't know how to read a node of type, %s" format node.getClass.getCanonicalName)
    }
  }
}