package ws.frontier

import com.twitter.finagle.http.{Request, Response}
import java.io.File
import org.apache.commons.cli.{HelpFormatter, CommandLine, PosixParser, Options}
import ws.frontier.core.{FrontierOptions, Frontier}
import ws.frontier.core.converter.FrontierMapper
import com.twitter.util.Future
import ws.frontier.core.util.Banner

/**
 * @author matt
 */

object FrontierApp {
  val cliOptions = {
    val options: Options = new Options()
    options.addOption("n", "no-cache", false, "don't cache templates")
    options.addOption("b", "banner", false, "display banner messages on startup")
    options.addOption("c", "config", true, "json configuration file")
    options.addOption("h", "help", false, "usage")
    options
  }

  def usage() {
    val formatter: HelpFormatter = new HelpFormatter
    println()
    formatter.printHelp("java -jar frontier.jar", cliOptions, true)
    println()
    System.exit(1)
  }

  def main(args: Array[String]) {
    val parser = new PosixParser()
    val cli: CommandLine = parser.parse(cliOptions, args)

    if (cli.hasOption("help")) {
      usage()
    }

    val config = new File(cli.getOptionValue("config", "frontier.json"))

    if (!config.exists()) {
      println()
      println("Unable to start Frontier!  Configuration file, %s, not found!" format config.getPath)
      usage()
    }

    val log: Banner = new Banner
    val frontier = FrontierMapper.readValue[Frontier[Request, Response]](config)

    var frontierOptions = FrontierOptions()
    if (cli.hasOption("no-cache")) {
      frontierOptions = frontierOptions.copy(cacheTemplates = false)
    }

    frontier.initialize(frontierOptions)

    if (cli.hasOption("banner")) {
      frontier.banner(log)
    }

    val results: Future[Seq[Int]] = frontier.start()

    results.onSuccess {
      ports => log("Frontier started successfully.  Listening to %s" format ports.mkString(", "))
    }

  }
}
