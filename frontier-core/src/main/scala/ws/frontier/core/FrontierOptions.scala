package ws.frontier.core

import java.text.NumberFormat
import ws.frontier.core.util.Banner

/**
 * @author matt
 */
case class FrontierOptions(
                            _cacheTemplates: Option[Boolean] = None,

                            /**
                             * [OPTIONAL] how long (in seconds) we should attempt a connection before timing out (defaults to 5 seconds)
                             */
                            _timeout: Option[Int] = None,

                            /**
                             * tcpConnectTimeout measured in seconds
                             */
                            _tcpConnectTimeout: Option[Int] = None,

                            /**
                             * handles deflate and gzip decompression.
                             *
                             * Note: i believe we want this to true so we can keep the data compressed as long as possible
                             */
                            _decompressionEnabled: Option[Boolean] = None,

                            /**
                             * measured in mb
                             */
                            _maxRequestSize: Option[Int] = None,

                            /**
                             * measured in mb
                             */
                            _maxResponseSize: Option[Int] = None,

                            /**
                             * [OPTIONAL] how many concurrent connections should we allow to the specified host (defaults to 1024)
                             */
                            _hostConnectionLimit: Option[Int] = None) {

  def cacheTemplates: Boolean = _cacheTemplates.getOrElse(true)

  def timeout: Int = _timeout.getOrElse(5)

  def tcpConnectTimeout: Int = _tcpConnectTimeout.getOrElse(5)

  def decompressionEnabled: Boolean = _decompressionEnabled.getOrElse(true)

  def maxRequestSize: Int = _maxRequestSize.getOrElse(10)

  def maxResponseSize: Int = _maxResponseSize.getOrElse(10)

  def hostConnectionLimit: Int = _hostConnectionLimit.getOrElse(1024)

  def banner(log: Banner) {
    val numberFormat = {
      val format = NumberFormat.getNumberInstance
      format.setGroupingUsed(true)
      format
    }

    val message = s"""
      | ProxyTrail {
      | timeout: ${timeout}s
      | tcpConnectTimeout: ${tcpConnectTimeout} s
      | hostConnectionLimit: ${numberFormat.format(hostConnectionLimit)}
      | decompressionEnabled: ${decompressionEnabled}
      | maxRequestSize: ${numberFormat.format(maxRequestSize)} M
      | maxResponseSize: ${ numberFormat.format(maxResponseSize) } M
      |
    """.stripMargin

    log("FrontierOptions: {")
    log.child(log(message))
    log("}")
  }

  /**
   * #zip provides a simple way to
   *
   * (a) construct a new FrontierOptions that
   * (b) allows you to override specified values but not others
   *
   * any value in the provided options object that has a Some value will be overridden.  None will be ignored
   *
   * @param options the values to overwrite with
   * @return a newly minted FrontierOptions instance
   */
  def zip(options: FrontierOptions): FrontierOptions = {
    /**
     * Logic: we're taking advantage of how scala case classes are constructed to merge two case classes together
     *
     * 1. for each element in the case class:
     * 1a. if options has a Some value, use it
     * 1b. otherwise, if this has a Some value, use it
     * 1c. otherwise None
     *
     * 2. the sum of all the productArity values will be the arguments to the constructor to make a new FrontierOptions
     */
    val productArity: Int = options.productArity
    val args = new Array[AnyRef](productArity)
    for (index <- 0 until productArity) {
      val value = options.productElement(index) match {
        case Some(a) => Some(a)
        case _ => this.productElement(index) match {
          case Some(b) => Some(b)
          case _ => None
        }
      }
      args(index) = value
    }
    val constructor = classOf[FrontierOptions].getConstructors.filter(_.getParameterTypes.length == productArity).head
    constructor.newInstance(args: _*).asInstanceOf[FrontierOptions]
  }
}

