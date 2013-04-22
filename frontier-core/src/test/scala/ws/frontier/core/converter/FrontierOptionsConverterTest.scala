package ws.frontier.core.converter

import ws.frontier.test.TestSuite
import ws.frontier.core.FrontierOptions
import java.lang.reflect.Constructor

/**
 * @author matt
 */

class FrontierOptionsConverterTest extends TestSuite {
  "#readValue" should "read default FrontierOptions" in {
    val options: FrontierOptions = FrontierMapper.readValue[FrontierOptions]("{}")
    options should not(be(null))
  }

  it should "handle cache_templates" in {
    val json = """{"cache_templates":false}"""
    val options: FrontierOptions = FrontierMapper.readValue[FrontierOptions](json)
    options.cacheTemplates should be(false)
  }

  it should "handle decompression_enabled" in {
    val json = """{"decompression_enabled":false}"""
    val options: FrontierOptions = FrontierMapper.readValue[FrontierOptions](json)
    options.decompressionEnabled should be(false)
  }

  it should "handle timeout" in {
    val json = """{"timeout":15}"""
    val options: FrontierOptions = FrontierMapper.readValue[FrontierOptions](json)
    options.timeout should be(15)
  }

  it should "handle tcp_connect_timeout" in {
    val json = """{"tcp_connect_timeout":15}"""
    val options: FrontierOptions = FrontierMapper.readValue[FrontierOptions](json)
    options.tcpConnectTimeout should be(15)
  }

  it should "handle max_request_size" in {
    val json = """{"max_request_size":15}"""
    val options: FrontierOptions = FrontierMapper.readValue[FrontierOptions](json)
    options.maxRequestSize should be(15)
  }

  it should "handle max_response_size" in {
    val json = """{"max_response_size":15}"""
    val options: FrontierOptions = FrontierMapper.readValue[FrontierOptions](json)
    options.maxResponseSize should be(15)
  }

  it should "handle host_connection_limit" in {
    val json = """{"host_connection_limit":15}"""
    val options: FrontierOptions = FrontierMapper.readValue[FrontierOptions](json)
    options.hostConnectionLimit should be(15)
  }

  it should "raise exception on unknown property" in {
    evaluating {
      FrontierMapper.readValue[FrontierOptions]( """{"this_is_junk":true}""")
    } should produce[RuntimeException]
  }

  it should "have a productArity of 7" in {
    val maxRequestSize: Int = 112
    val maxResponseSize: Int = 125

    val a = FrontierOptions(_maxRequestSize = Some(maxRequestSize + 1), _cacheTemplates = Some(false))
    val b = FrontierOptions(_maxRequestSize = Some(maxRequestSize), _maxResponseSize = Some(maxResponseSize))
    val c = b.zip(a)
    c.cacheTemplates should be(false)
    c.maxRequestSize should be(maxRequestSize + 1)
    c.maxResponseSize should be(maxResponseSize)
  }
}
