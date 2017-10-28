package com.micronautics.cli

import java.util.{Map => JMap}
import org.eclipse.jgit.lib.Repository
import org.eclipse.jgit.storage.file.FileRepositoryBuilder
import org.jline.keymap.KeyMap
import org.jline.reader.impl.{DefaultParser, LineReaderImpl}
import org.jline.reader.{Binding, EndOfFileException, LineReader, LineReaderBuilder, Macro, ParsedLine, Reference, UserInterruptException}
import org.jline.terminal.{Terminal, TerminalBuilder}
import org.jline.utils.InfoCmp.Capability
import org.jline.utils.{AttributedStringBuilder, AttributedStyle}
import scala.collection.JavaConverters._

object CliLoop {
  protected val parser: DefaultParser = new DefaultParser
  parser.setEofOnUnclosedQuote(true)

  // todo how to obtain the following list from the completer?
  protected val commands = List("account", "bindkey", "javascript", "exit", ("help", "?"), "password", "set", "testkey", "tput")

  protected val cmdMaxWidth: Int = commands.map {
    case string: String => string.length
    case (name: String, alias: String) => name.length + alias.length + 1
  }.max

  protected val defaultStyle: AttributedStyle = AttributedStyle.DEFAULT
  protected val errorStyle: AttributedStyle = AttributedStyle.DEFAULT.foreground(AttributedStyle.RED)
  protected val helpStyle: AttributedStyle = AttributedStyle.DEFAULT.foreground(AttributedStyle.CYAN)
  protected val infoStyle: AttributedStyle = AttributedStyle.DEFAULT.foreground(AttributedStyle.MAGENTA)

  val terminal: Terminal =
    TerminalBuilder.builder
      .system(true)
      .build

  // todo test terminal capabilities to see how many of these styles are supported
  protected def bold(name: String, isPenultimate: Boolean = false, isLast: Boolean = false)
                    (implicit asb: AttributedStringBuilder): AttributedStringBuilder = {
    asb
      .style(AttributedStyle.DEFAULT.foreground(AttributedStyle.YELLOW))
      .append(name)
      .style(AttributedStyle.DEFAULT.foregroundDefault)
      .style(AttributedStyle.DEFAULT.faint)

    if (isPenultimate)
      asb.append(" and ")
    else if (!isLast)
      asb.append(", ")

    asb.style(AttributedStyle.DEFAULT.faintDefault)
  }

  // todo test terminal capabilities to see how many of these styles are supported
  protected def bold(name: String, alias: String)
                    (implicit asb: AttributedStringBuilder): AttributedStringBuilder =
    asb
      .style(AttributedStyle.DEFAULT.foreground(AttributedStyle.YELLOW))
      .append(name)
      .style(AttributedStyle.DEFAULT.faint)
      .append("/")
      .style(AttributedStyle.DEFAULT.faintDefault)
      .style(AttributedStyle.DEFAULT.foreground(AttributedStyle.GREEN))
      .append(alias)
      .style(AttributedStyle.DEFAULT.foregroundDefault)
      .style(AttributedStyle.DEFAULT.faint)
      .append(", ")
      .style(AttributedStyle.DEFAULT.faintDefault)

  @inline def printRichError(message: String): Unit = terminal.writer.println(richError(message))

  @inline def printRichInfo(message: String): Unit = terminal.writer.println(info(message))

  // todo test terminal capabilities to see how many of these styles are supported
  def richError(message: String): String =
    new AttributedStringBuilder()
      .style(errorStyle)
      .append(" Error: " + message + " ")
      .style(defaultStyle)
      .toAnsi

  // todo test terminal capabilities to see how many of these styles are supported
  def info(message: String): String =
    new AttributedStringBuilder()
      .style(infoStyle)
      .append(message)
      .style(defaultStyle)
      .toAnsi

  protected def gitRepo: Repository = FileRepositoryBuilder.create(new java.io.File(".git/").getAbsoluteFile)

  protected def gitBranch: String = gitRepo.getBranch

  // todo add parameters for helpCmd name/value tuples/triples
  protected def help(full: Boolean = false): Unit = {
    /** This implicit acts as a local accumulator for the rich help message */
    implicit val asb: AttributedStringBuilder =
      new AttributedStringBuilder()
        .style(helpStyle)
        .append("Commands are: ")

    commands.zipWithIndex.foreach { case (x, i) =>
      x match {
        case name: String if i==commands.length-1 => bold(name, isLast=true)
        case name: String if i==commands.length-2 => bold(name, isPenultimate=true)
        case name: String => bold(name)
        case (name: String, alias: String) => bold(name, alias)
      }
    }

    asb.style(defaultStyle)
    terminal.writer.println(asb.toAnsi)

    if (full) {
      /** This implicit acts as a local accumulator for the rich help message */
      implicit val asb: AttributedStringBuilder = new AttributedStringBuilder()
        .append("\n")
        .style(helpStyle)

      helpCmd("account",     "Ethereum account management")
      helpCmd("bindkey",     "Show all key bindings")
      helpCmd("javascript",  "Enter JavaScript console")
      helpCmd("help", "?",   "Display this message")
      helpCmd("set",        s"Set a terminal variable, such as '${ LineReader.PREFER_VISIBLE_BELL }'")
      helpCmd("testkey",     "Test a key binding")
      helpCmd("tput",        "Demonstrate a terminal capability, such as 'bell'")

      asb.style(defaultStyle)
      terminal.writer.println(asb.toAnsi)
    }
  }

  // todo test terminal capabilities to see how many of these styles are supported
  protected def helpCmd(name: String, message: String)
                       (implicit attributedStringBuilder: AttributedStringBuilder): AttributedStringBuilder =
    attributedStringBuilder
      .style(AttributedStyle.DEFAULT.bold)
      .append(name + " "*(cmdMaxWidth-name.length))
      .style(AttributedStyle.DEFAULT.boldOff)
      .append(" - ")
      .append(message + "\n")

  // todo test terminal capabilities to see how many of these styles are supported
  protected def helpCmd(name: String, alias: String, message: String)
                       (implicit attributedStringBuilder: AttributedStringBuilder): AttributedStringBuilder =
    attributedStringBuilder
      .style(AttributedStyle.DEFAULT.bold)
      .append(name)
      .style(AttributedStyle.DEFAULT.boldOff)
      .append("/")
      .style(AttributedStyle.DEFAULT.bold)
      .append(alias)
      .style(AttributedStyle.DEFAULT.boldOff)
      .append(" "*(cmdMaxWidth-name.length - alias.length - 1) + " - ")
      .append(message)
      .append("\n")
}

class CliLoop(promptName: String) extends CommandCompleter with SampleArgumentCompleter {
  import com.micronautics.cli.CliLoop._

  protected val useColor = true
  var mode: Mode = Mode.COMMAND

  // todo test terminal capabilities to see how many of these styles are supported
  protected def prompt: String = new AttributedStringBuilder()
    .style(AttributedStyle.DEFAULT.foreground(AttributedStyle.GREEN))
    .append(promptName)
    .style(AttributedStyle.DEFAULT.foreground(AttributedStyle.YELLOW))
    .append(s" [$gitBranch]")
    .style(AttributedStyle.DEFAULT.foreground(AttributedStyle.GREEN))
    .append(s" ${ mode.name.toLowerCase }")
    .style(AttributedStyle.DEFAULT.foreground(AttributedStyle.MAGENTA))
    .append("> ")
    .style(AttributedStyle.DEFAULT.foregroundDefault)
    .toAnsi

  // todo probably make this overridable or configurable
  protected val reader: LineReader = LineReaderBuilder.builder
    .terminal(terminal)
    .completer(treeCompleter)
    .parser(parser)
    .build

  def run(): Unit = {
    help()
    loop()
  }

  protected def loop(): Unit = {
    val trigger: Option[String] = Some("password")
    val mask: Character = null  // LineReader.readLine uses null to control behavior // TODO set this value
    var more = true

    while (more) {
      var line: String =
        try {
          reader.readLine(prompt)
        } catch {
          case _: UserInterruptException =>
            printRichInfo("Press Control-D to exit.")
            ""

          case _: EndOfFileException =>
            mode match {
              case Mode.COMMAND =>
                printRichInfo("\nExiting program.")
                System.exit(0)

              case Mode.JAVASCRIPT =>
                printRichInfo("\nReturning to command mode.")
                help()
                mode = Mode.COMMAND
            }
            ""
        }

      if (line != null && line.trim.nonEmpty) {
        line = line.trim

        // If the trigger word is input then the next input line is masked
        if (trigger.exists(line.compareTo(_) == 0))
          line = reader.readLine("password> ", mask)

        if (line.equalsIgnoreCase("quit") || line.equalsIgnoreCase("exit"))
          more = false
        else
          processLine(line)
      }
    }
  }

  protected def processLine(line: String): Unit = {
    val parsedLine: ParsedLine = reader.getParser.parse(line, 0)
    parsedLine.word match {
      case "account" => account(parsedLine)

      case "bindkey" => bindKey(parsedLine)

      case "javascript" =>
        mode = Mode.JAVASCRIPT
        printRichInfo("Entering JavaScript mode. Press Control-d to return to command mode.")
        new JavaScript().peekPoke()

      case "help" | "?" =>
        terminal.writer.println()
        help(true)

      case "set" => set(parsedLine)

      case "testkey" => testKey()

      case "tput" => tput(parsedLine)

      case "" =>
        terminal.writer.println()
        help()

      case x =>
        printRichError(s"'$x' is an unknown command.")
        help()
    }
  }

  protected def account(pl: ParsedLine): Unit =
    terminal.writer.println(
      s"""parsedLine: word = ${ pl.word }, wordIndex = ${ pl.wordIndex }, wordCursor = ${ pl.wordCursor }, cursor = ${ pl.cursor }
         |words = ${ pl.words.asScala.mkString(", ") }
         |line = ${ pl.line }
         |""".stripMargin
    )

  protected def bindKey(parsedLine: ParsedLine): Unit = {
    if (parsedLine.words.size == 1) {
      val sb = new StringBuilder
      val bound: JMap[String, Binding] = reader.getKeys.getBoundKeys
      bound.entrySet.forEach { entry =>
        sb.append("\"")
        entry.getKey.chars.forEachOrdered { c =>
          if (c < 32) {
            sb.append('^')
            sb.append((c + 'A' - 1).asInstanceOf[Char])
          } else
            sb.append(c.asInstanceOf[Char])
          ()
        }
        sb.append("\" ")
        entry.getValue match {
          case value: Macro =>
            sb.append("\"")
            value.getSequence.chars.forEachOrdered { c =>
              if (c < 32) {
                sb.append('^')
                sb.append((c + 'A' - 1).asInstanceOf[Char])
              } else
                sb.append(c.asInstanceOf[Char])
              ()
            }
            sb.append("\"")

          case reference: Reference =>
            sb.append(reference.name.toLowerCase.replace('_', '-'))

          case _ =>
            sb.append(entry.getValue.toString)
        }
        sb.append("\n")
        ()
      }
      terminal.writer.print(sb.toString)
      terminal.flush()
    } else if (parsedLine.words.size == 3) {
      reader.getKeys.bind(
        new Reference(parsedLine.words.get(2)), KeyMap.translate(parsedLine.words.get(1))
      )
    }
  }

  protected def set(parsedLine: ParsedLine): Unit = {
    parsedLine.words.size match {
      case 1 =>
        printRichError("\nNo variable name or value specified")

      case 2 =>
        printRichError("\nNo new value specified for " + parsedLine.words.get(0))

      case 3 =>
        reader.setVariable(parsedLine.words.get(0), parsedLine.words.get(1))

      case n =>
        printRichError("\nOnly one new value may be specified " +
          s"(you specified ${n - 2} values for ${parsedLine.words.get(0)})")
    }
  }

  protected def testKey(): Unit = {
    terminal.writer.write("Input the key event (Enter to complete): ")
    terminal.writer.flush()
    val sb = new StringBuilder
    var more = true
    while (more) {
      val c: Int = reader.asInstanceOf[LineReaderImpl].readCharacter
      if (c == 10 || c == 13) more = false
      else sb.append(new String(Character.toChars(c)))
    }
    terminal.writer.println(KeyMap.display(sb.toString))
    terminal.writer.flush()
  }

  protected def tput(parsedLine: ParsedLine): Unit = parsedLine.words.size match {
    case 1 =>
      printRichError("No capability specified (try 'bell')")

    case 2 =>
      Option(Capability.byName(parsedLine.words.get(1))).map { capability =>
        terminal.puts(capability)
        terminal.flush()
        true
      }.getOrElse {
        printRichError("Unknown capability")
        false
      }
      ()

    case n =>
      printRichError(s"Only one capability may be specified (you specified ${ n - 1 })")
  }
}
