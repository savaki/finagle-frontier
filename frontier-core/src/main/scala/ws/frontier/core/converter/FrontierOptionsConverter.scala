package ws.frontier.core.converter

import com.fasterxml.jackson.databind.{JsonNode, DeserializationContext, JsonDeserializer}
import ws.frontier.core.FrontierOptions
import com.fasterxml.jackson.core.JsonParser
import scala.collection.JavaConversions._

/**
 * @author matt
 */

class FrontierOptionsConverter extends JsonDeserializer[FrontierOptions] {
  def deserialize(parser: JsonParser, context: DeserializationContext): FrontierOptions = {
    def asInt(implicit fieldName: String, node: JsonNode): Option[Int] = {
      Option(node.get(fieldName).asInt())
    }

    def asBoolean(implicit fieldName: String, node: JsonNode): Option[Boolean] = {
      Option(node.get(fieldName).asBoolean())
    }

    var options = FrontierOptions()
    implicit val node: JsonNode = parser.readValueAsTree()
    node.fieldNames().foreach {
      implicit fieldName =>
        options = fieldName match {
          case "cache_templates" => options.copy(_cacheTemplates = asBoolean)
          case "decompression_enabled" => options.copy(_decompressionEnabled = asBoolean)
          case "timeout" => options.copy(_timeout = asInt)
          case "tcp_connect_timeout" => options.copy(_tcpConnectTimeout = asInt)
          case "max_request_size" => options.copy(_maxRequestSize = asInt)
          case "max_response_size" => options.copy(_maxResponseSize = asInt)
          case "host_connection_limit" => options.copy(_hostConnectionLimit = asInt)
          case anythingElse => throw new RuntimeException("unknown option parameter, %s" format anythingElse)
        }
    }
    options
  }
}