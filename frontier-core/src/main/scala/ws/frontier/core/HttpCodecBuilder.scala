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
  def buildCodec(options: FrontierOptions): Http = {
    val codec: Http = Http()
    codec.decompressionEnabled(options.decompressionEnabled)
    codec.maxRequestSize(options.maxRequestSize.megabytes)
    codec.maxResponseSize(options.maxResponseSize.megabytes)
    codec
  }
}
