package ws.frontier.core

/**
 * @author matt
 */

class Banner(tab: String = "  ") {
  private[this] var depth = 0

  /**
   * indicate that the following content should be rendered as a child of the current content
   *
   * @param callback
   */
  def child(callback: => Any) {
    depth = depth + 1
    callback
    depth = depth - 1
  }

  def apply(message: String = "") {
    Option(message).getOrElse("").split("\n").foreach {
      line => println(s"${
        tab * depth
      }${line}")
    }
  }
}
