package ws.frontier.core.converter

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind._
import ws.frontier.core.{HttpProxyTrail, AggregatingTrail, Trail}
import scala.collection.JavaConversions._
import java.util

/**
 * @author matt.ho@gmail.com
 */
class TrailConverter extends JsonDeserializer[Trail[_, _]] {
  def deserialize(parser: JsonParser, context: DeserializationContext): Trail[_, _] = {
    val node: JsonNode = parser.readValueAsTree()
    val mapper: ObjectMapper = parser.getCodec.asInstanceOf[ObjectMapper]
    deserialize(mapper, node)
  }

  def deserialize[IN, OUT](mapper: ObjectMapper, node: JsonNode): Trail[IN, OUT] = {
    val trail: Trail[IN, OUT] = if (node.isArray) {
      val trails = node.iterator().map(deserialize[IN, OUT](mapper, _)).toArray
      new AggregatingTrail[IN, OUT](trails)

    } else {
      val reader: ObjectReader = mapper.reader(classOf[HttpProxyTrail])
      reader.readValue[Trail[IN, OUT]](node)
    }

    TrailConverter.capture(trail.id, trail)

    trail
  }
}

object TrailConverter {
  type TrailMap = util.HashMap[String, Trail[_, _]]

  private[this] val trailMapHolder = new ThreadLocal[TrailMap]

  def capture(id: String, trail: Trail[_, _]) {
    if (id != null && trailMapHolder.get() != null) {
      trailMapHolder.get().put(id, trail)
    }
  }

  def captureTrails[T](function: => T): (T, TrailMap) = {
    val original = trailMapHolder.get()
    val trailMap = new TrailMap()
    trailMapHolder.set(trailMap)
    try {
      val result = function
      (result, trailMap)

    } finally {
      trailMapHolder.set(original)
    }
  }
}

