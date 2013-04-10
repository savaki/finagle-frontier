package ws.frontier.core.util

import org.jboss.netty.util.HashedWheelTimer
import com.twitter.finagle.util.TimerFromNettyTimer

/**
 * @author matt.ho@gmail.com
 */
object Timer extends TimerFromNettyTimer(new HashedWheelTimer) {

}
