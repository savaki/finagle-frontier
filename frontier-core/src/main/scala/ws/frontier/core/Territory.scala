package ws.frontier.core

import beans.BeanProperty

/**
 * @author matt.ho@gmail.com
 */
class Territory[IN, OUT] {
  @BeanProperty
  var port: Int = 9080

  @BeanProperty
  var trails: Array[Trail[IN, OUT]] = null
}
