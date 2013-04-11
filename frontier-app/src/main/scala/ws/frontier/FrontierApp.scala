package ws.frontier

import com.twitter.finagle.http.{Request, Response}
import java.io.File
import org.apache.commons.cli.{HelpFormatter, CommandLine, PosixParser, Options}
import ws.frontier.core.{Banner, Frontier}
import ws.frontier.core.converter.FrontierMapper

/**
 * @author matt
 */

object FrontierApp {
  val options = {
    val options: Options = new Options()
    options.addOption("c", "config", true, "json configuration file")
    options.addOption("h", "help", false, "usage")
    options
  }

  def usage() {
    val formatter: HelpFormatter = new HelpFormatter
    println()
    formatter.printHelp("java -jar frontier.jar", options, true)
    println()
    System.exit(1)
  }

  def main(args: Array[String]) {
    val parser = new PosixParser()
    val cli: CommandLine = parser.parse(options, args)

    if (cli.hasOption("help")) {
      usage()
    }

    val config = new File(cli.getOptionValue("config", "frontier.json"))

    if (!config.exists()) {
      println()
      println("Unable to start Frontier!  Configuration file, %s, not found!" format config.getPath)
      usage()
    }

    val frontier = FrontierMapper.readValue[Frontier[Request, Response]](config)
    frontier.initialize()
    frontier.banner(new Banner)
    frontier.start()

    println("hello world")
  }
}
