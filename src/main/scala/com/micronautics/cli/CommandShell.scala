package com.micronautics.cli

import collection.JavaConverters._
import com.micronautics.cli.CliLoop.{defaultStyle, helpCmd, helpStyle, printRichError, printRichHelp, printRichInfo, terminal}
import java.util.{Map => JMap}
import org.jline.keymap.KeyMap
import org.jline.reader.{Binding, LineReader, Macro, ParsedLine, Reference}
import org.jline.reader.impl.LineReaderImpl
import org.jline.utils.AttributedStringBuilder
import org.jline.utils.InfoCmp.Capability

trait CliImpl extends CliBase {
  // todo how to obtain the following list from the completer?
  lazy val commands: List[AnyRef] =
    List("account", "bindkey", ("exit", "^d"), "javascript", ("help", "?"), "password", "set", "testkey", "tput")
}

object CommandShell extends Completers

class CommandShell extends CliLoop("beth") {
  var mode: Mode = Mode.COMMAND

  protected def fullHelp(): Unit = {

    /** This implicit acts as a local accumulator for the rich help message */
    implicit val asb: AttributedStringBuilder = new AttributedStringBuilder().append("\n")

    if (TerminalCapabilities.supportsAnsi) asb.style(helpStyle)

    helpCmd("account",     "Ethereum account management")
    helpCmd("bindkey",     "Show all key bindings")
    helpCmd("javascript",  "Enter JavaScript console")
    helpCmd("help", "?",   "Display this message")
    helpCmd("set",        s"Set a terminal variable, such as '${ LineReader.PREFER_VISIBLE_BELL }'")
    helpCmd("testkey",     "Test a key binding")
    helpCmd("tput",        "Demonstrate a terminal capability, such as 'bell'")

    if (TerminalCapabilities.supportsAnsi) asb.style(defaultStyle)
    terminal.writer.println(asb.toAnsi)
  }

  protected def processCommandLine(line: String): Unit = {
    val parsedLine: ParsedLine = reader.getParser.parse(line, 0)
    parsedLine.word match {
      case "account" => account(parsedLine)

      case "bindkey" => bindKey(parsedLine)

      case "javascript" =>
        mode = Mode.JAVASCRIPT
        printRichInfo("Entering JavaScript mode. Press Control-d to return to command mode.\n")

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

  protected def processJavaScriptLine(line: String): Unit = {
    val result: AnyRef = javaScript.eval(line)
    printRichInfo(result.toString)
    ()
  }

  def signInMessage(): Unit = printRichHelp("Press <tab> for tab completion of commands and options.\n")

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
