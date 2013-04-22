package ws.frontier.core

import com.twitter.finagle.http.Http
import com.twitter.conversions.storage._

/**
 * @author matt
 */

trait HttpCodecBuilder {
  /**
   * utility method to build the Http codec with all the extra parameters we may want
   */
  def buildHttpCodec(options: FrontierOptions): Http = {
    Http(
      _decompressionEnabled = options.decompressionEnabled,
      _maxHeaderSize = options.maxHeaderSize.megabytes,
      _maxRequestSize = options.maxRequestSize.megabytes,
      _maxResponseSize = options.maxResponseSize.megabytes
    )
  }
}
