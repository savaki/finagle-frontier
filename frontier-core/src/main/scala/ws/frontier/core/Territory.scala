package ws.frontier.core

import beans.BeanProperty
import com.twitter.finagle.Service
import com.twitter.finagle.builder.{Server, ServerBuilder}
import com.twitter.finagle.http.RichHttp
import com.twitter.finagle.http.{Response, Http, Request}
import com.twitter.util.Future
import java.lang.reflect.Method
import java.net.InetSocketAddress
import java.util.{Set => JSet}
import javax.validation._
import javax.validation.constraints.NotNull
import scala.collection.JavaConversions._
import scala.collection.JavaConverters._

/**
 * @author matt.ho@gmail.com
 */
class Territory[IN, OUT] {
  @BeanProperty
  var port: Int = 9080

  @NotNull
  @BeanProperty
  var name: String = null

  @Valid
  @NotNull
  @BeanProperty
  var trail: Trail[Request, Response] = null

  private[this] var server: Server = null

  def validate() {
    val validator: Validator = Validation.buildDefaultValidatorFactory().getValidator
    val isGetter: (Method) => Boolean = {
      method => method.getName.startsWith("get") && method.getName.length > "get".length
    }
    val ignoreObjectMethods: (Method) => Boolean = {
      method => classOf[Object].getMethods.map(_.getName).contains(method.getName) == false
    }
    val ignorePrimitives: (Method) => Boolean = {
      method => {
        val returnType: Class[_] = method.getReturnType
        classOf[Object].isAssignableFrom(returnType) &&
          classOf[String].isAssignableFrom(returnType) == false &&
          classOf[Number].isAssignableFrom(returnType) == false
      }
    }

    def handleValidation(target: AnyRef): JSet[ConstraintViolation[_]] = {
      val violations = validator.validate(target).asInstanceOf[JSet[ConstraintViolation[_]]]

      // find the acceptable getter methods for the class
      val getters = target.getClass.getMethods
        .filter(isGetter)
        .filter(ignoreObjectMethods)
        .filter(ignorePrimitives)

      // for each getter method, descend into the child and make sure it has no violations
      val childViolations = getters.flatMap {
        getter =>
          val child: AnyRef = getter.invoke(target)
          if (child != null) {
            if (child.getClass.isArray) {
              child.asInstanceOf[Array[AnyRef]].flatMap {
                item => handleValidation(item).toSet
              }.toSet.asJava

            } else {
              handleValidation(child).toSet
            }

          } else {
            Set[ConstraintViolation[_]]()
          }
      }

      violations ++ childViolations
    }

    val violations = validator.validate(this).asInstanceOf[JSet[ConstraintViolation[_]]]
    //    val violations = handleValidation(this)
    if (violations.size() > 0) {
      throw new ConstraintViolationException(violations)
    }
  }

  def initialize(): Future[Unit] = trail.initialize()

  def start(): Future[Unit] = {
    trail.start().map {
      unit =>
        server = ServerBuilder()
          .name("Frontier-%s" format port)
          .codec(RichHttp[Request](Http()))
          .bindTo(new InetSocketAddress(port))
          .build(new TrailService[Request, Response](trail))
    }
  }

  def shutdown(): Future[Unit] = {
    trail.shutdown().map {
      unit => server.close()
    }.map {
      unit => server = null
    }
  }
}

class TrailService[IN, OUT](trail: Trail[IN, OUT]) extends Service[IN, OUT] {
  def apply(request: IN): Future[OUT] = {
    trail(request).get
  }
}
