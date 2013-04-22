package ws.frontier.core

import ws.frontier.test.TestSuite
import com.twitter.conversions.storage._

/**
 * @author matt
 */

class HttpCodecBuilderTest extends TestSuite {
  val codec: HttpCodecBuilder = new Object with HttpCodecBuilder

  "#buildHttpCodec" should "read decompressionEnabled from options" in {
    codec.buildHttpCodec(FrontierOptions(_decompressionEnabled = Some(false)))._decompressionEnabled should be(false)
    codec.buildHttpCodec(FrontierOptions(_decompressionEnabled = Some(true)))._decompressionEnabled should be(true)
  }

  it should "read maxHeaderSize from options" in {
    val headerSize: Int = 15
    codec.buildHttpCodec(FrontierOptions(_maxHeaderSize = Some(headerSize)))._maxHeaderSize should be(headerSize.megabytes)
  }

  it should "read maxRequestSize from options" in {
    val headerSize: Int = 15
    codec.buildHttpCodec(FrontierOptions(_maxRequestSize = Some(headerSize)))._maxRequestSize should be(headerSize.megabytes)
  }

  it should "read maxResponseSize from options" in {
    val headerSize: Int = 15
    codec.buildHttpCodec(FrontierOptions(_maxResponseSize = Some(headerSize)))._maxResponseSize should be(headerSize.megabytes)
  }
}