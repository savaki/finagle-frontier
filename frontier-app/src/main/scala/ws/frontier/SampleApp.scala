package ws.frontier

/**
 * @author matt
 */

object SampleApp {
  def main(args: Array[String]) {
    FrontierApp.main(Array(
      "--config",
      "dev.json",
      "--banner"
    ))
  }
}