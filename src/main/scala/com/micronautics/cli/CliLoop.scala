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
  protected val commands = List("account", "bindkey", "console", ("help", "?"), "password", "set", "testkey", "tput")

  protected val cmdMaxWidth: Int = commands.map {
    case string: String => string.length
    case (name: String, alias: String) => name.length + alias.length + 1
  }.max

  val terminal: Terminal =
    TerminalBuilder.builder
      .system(true)
      .build

  protected def bold(name: String, isPenultimate: Boolean = false, isLast: Boolean = false)
                       (implicit attributedStringBuilder: AttributedStringBuilder): AttributedStringBuilder = {
    attributedStringBuilder
      .style(AttributedStyle.DEFAULT.bold)
      .append(name)
      .style(AttributedStyle.DEFAULT)

    if (isPenultimate)
      attributedStringBuilder.append(" and ")
    else if (!isLast)
      attributedStringBuilder.append(", ")

    attributedStringBuilder
  }

  protected def bold(name: String, alias: String)
                       (implicit attributedStringBuilder: AttributedStringBuilder): AttributedStringBuilder =
    attributedStringBuilder
      .style(AttributedStyle.DEFAULT.bold)
      .append(name)
      .style(AttributedStyle.DEFAULT)
      .append("/")
      .style(AttributedStyle.DEFAULT.bold)
      .append(alias)
      .style(AttributedStyle.DEFAULT)
      .append(", ")

  def error(message: String): String =
    new AttributedStringBuilder()
      .style(AttributedStyle.DEFAULT.foreground(AttributedStyle.RED))
      .append(" Error: " + message + " ")
      .style(AttributedStyle.DEFAULT)
      .toAnsi

  protected def repo: Repository = FileRepositoryBuilder.create(new java.io.File(".git/").getAbsoluteFile)

  protected def gitBranch: String = repo.getBranch

  protected def help(full: Boolean = false): Unit = {
    implicit val asb: AttributedStringBuilder = new AttributedStringBuilder().append("Commands are: ")
    commands.zipWithIndex.foreach { case (x, i) =>
      x match {
        case name: String if i==commands.length-1 => bold(name, isLast=true)
        case name: String if i==commands.length-2 => bold(name, isPenultimate=true)
        case name: String => bold(name)
        case (name: String, alias: String) => bold(name, alias)
      }
    }
    terminal.writer.println(asb.toAnsi)

    if (full) {
      implicit val asb: AttributedStringBuilder = new AttributedStringBuilder().append("\n")
      helpCmd("bindkey",   "shows all key bindings")
      helpCmd("console",   "display JavaScript console")
      helpCmd("account",   "Ethereum account management")
      helpCmd("help", "?", "displays this message")
      helpCmd("set",       s"set a terminal variable, such as '${ LineReader.PREFER_VISIBLE_BELL }'")
      helpCmd("testkey",   "tests a key binding")
      helpCmd("tput",      "demonstrates a terminal capability, such as 'bell'")
      terminal.writer.println(asb.toAnsi)
    }
  }

  protected def helpCmd(name: String, message: String)
                       (implicit attributedStringBuilder: AttributedStringBuilder): AttributedStringBuilder =
    attributedStringBuilder
      .style(AttributedStyle.DEFAULT.bold)
      .append(name + " "*(cmdMaxWidth-name.length))
      .style(AttributedStyle.DEFAULT)
      .append(" - ")
      .append(message)
      .append("\n")

  protected def helpCmd(name: String, alias: String, message: String)
                       (implicit attributedStringBuilder: AttributedStringBuilder): AttributedStringBuilder =
    attributedStringBuilder
      .style(AttributedStyle.DEFAULT.bold)
      .append(name)
      .style(AttributedStyle.DEFAULT)
      .append("/")
      .style(AttributedStyle.DEFAULT.bold)
      .append(alias)
      .style(AttributedStyle.DEFAULT)
      .append(" "*(cmdMaxWidth-name.length - alias.length - 1) + " - ")
      .append(message)
      .append("\n")

}

class CliLoop(promptName: String) extends CommandCompleter with SampleArgumentCompleter {
  import com.micronautics.cli.CliLoop._

  protected val useColor = true
  var mode: Mode = Mode.COMMAND

  protected def prompt: String = new AttributedStringBuilder()
    .style(AttributedStyle.DEFAULT.background(AttributedStyle.GREEN))
    .append(promptName)
    .style(AttributedStyle.DEFAULT)
    .append(s" [$gitBranch]")
    .style(AttributedStyle.DEFAULT.foreground(AttributedStyle.GREEN))
    .append(s" ${ mode.name }")
    .style(AttributedStyle.DEFAULT)
    .append("> ")
    .toAnsi

  protected val reader: LineReader = LineReaderBuilder.builder
    .terminal(terminal)
    .completer(treeCompleter)
    .parser(parser)
    .build

  def run(): Unit = {
    help()
    loop()
  }

  def loop(): Unit = {
    val trigger: Option[String] = Some("password")
    val mask: Character = null  // LineReader.readLine uses null to control behavior // TODO set this value
    var more = true

    while (more) {
      var line: String =
        try {
          reader.readLine(prompt)
        } catch {
          case _: UserInterruptException =>
            terminal.writer.println("Press Control-D to exit")
            ""

          case _: EndOfFileException =>
            mode match {
              case Mode.COMMAND =>
                terminal.writer.println("\nExiting program")
                System.exit(0)

              case Mode.JAVASCRIPT =>
                terminal.writer.println("\nReturning to command mode")
                mode = Mode.COMMAND
            }
            ""
        }

      if (line != null) {
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

      case "console" =>
        mode = Mode.JAVASCRIPT
        terminal.writer.println("\nEntering JavaScript mode")
        new JavaScript().demo()

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
        terminal.writer.println(s"Error: '$x' is an unknown command.")
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
        terminal.writer.println("\nError: no variable name or value specified")

      case 2 =>
        terminal.writer.println("\nError: no new value specified for " + parsedLine.words.get(0))

      case 3 =>
        reader.setVariable(parsedLine.words.get(0), parsedLine.words.get(1))

      case n =>
        terminal.writer.println("\nError: Only one new value may be specified " +
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
      terminal.writer.println("No capability specified (try 'bell')")

    case 2 =>
      Option(Capability.byName(parsedLine.words.get(1))).map { capability =>
        terminal.puts(capability)
        terminal.flush()
        true
      }.getOrElse {
        terminal.writer.println("Unknown capability")
        false
      }
      ()

    case n =>
      terminal.writer.println(s"Only one capability may be specified (you specified ${ n - 1 })")
  }
}
