package ws.frontier.core.trail.converter

import com.fasterxml.jackson.databind._
import com.fasterxml.jackson.core.{ObjectCodec, JsonParser}
import scala.collection.JavaConversions._
import ws.frontier.core.config.FrontierMapper
import ws.frontier.core.{HttpProxyTrail, AggregatingTrail, Trail}

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
    if (node.isArray) {
      val trails = node.iterator().map(deserialize[IN, OUT](mapper, _)).toSeq
      new AggregatingTrail[IN, OUT](trails: _*)

    } else {
      val reader: ObjectReader = mapper.reader(classOf[HttpProxyTrail])
      reader.readValue[Trail[IN, OUT]](node)
    }
  }
}

object Foo {
  def main(args: Array[String]) {
    val json =
      """
        | [
        | {
        |   "hosts":["www.loyal3.com:80"]
        | }
        | ]
        | """.stripMargin
    val value: Trail[_, _] = FrontierMapper.readValue[Trail[_, _]](json)
    println(value.getClass.getCanonicalName)
    println(value.asInstanceOf[AggregatingTrail[_, _]].trails.head)
    //    val converter = new TrailConverter
    //    converter.
  }
}
