import java.util.{Map => JMap}
import org.jline.keymap.KeyMap
import org.jline.reader.impl.{DefaultParser, LineReaderImpl}
import org.jline.reader.{Binding, EndOfFileException, LineReader, LineReaderBuilder, Macro, MaskingCallback, ParsedLine, Reference, UserInterruptException}
import org.jline.terminal.{Terminal, TerminalBuilder}
import org.jline.utils.AttributedString
import org.jline.utils.InfoCmp.Capability

class CliLoop extends ComplexStringCompleter
  with CustomCompleter
  with MiscCompleters
  with SampleArgumentCompleter
  with SampleRegexCompleter
  with SampleTreeCompleter {
  protected val color = true

  protected val terminal: Terminal =
    TerminalBuilder.builder
      .system(true)
      .build

  protected val parser: DefaultParser = new DefaultParser
  parser.setEofOnUnclosedQuote(true)

  protected val reader: LineReader = LineReaderBuilder.builder
    .terminal(terminal)
    .completer(treeCompleter)
    .parser(parser)
    .build

  def loop(): Unit = {
    val trigger: Option[String] = None // TODO set this value
    val mask: Character = null  // LineReader.readLine uses null to control behavior // TODO set this value
    var more = true

    while (more) {
      var line: String = ""
      try {
        line = reader.readLine(prompt, rightPrompt, null.asInstanceOf[MaskingCallback], null)
      } catch {
        case _: UserInterruptException => // Ignore
        case _: EndOfFileException => return
      }
      if (line != null) {
        line = line.trim
        showPrompt(line)

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
      case "set" =>
        if (parsedLine.words.size == 3)
          reader.setVariable(parsedLine.words.get(1), parsedLine.words.get(2))

      case "tput" => tput(parsedLine)

      case "testkey" => testKey()

      case "bindkey" => bindKey(parsedLine)

      case "cls" =>
        terminal.puts(Capability.clear_screen)
        terminal.flush()

      case "sleep" => Thread.sleep(3000)

      case "" => println()

      case x => println(s"Error: '$x' is an unknown command.")
    }
  }

  protected def bindKey(pl: ParsedLine): Unit = {
    if (pl.words.size == 1) {
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
    } else if (pl.words.size == 3) {
      reader.getKeys.bind(
        new Reference(pl.words.get(2)), KeyMap.translate(pl.words.get(1))
      )
    }
  }

  protected def showPrompt(line: String): Unit = {
    terminal.writer.println(
      if (color) {
        AttributedString.fromAnsi(s"""\u001B[33m======>\u001B[0m"$line"""").toAnsi(terminal)
      } else {
        s"""======>"$line""""
      }
    )
    terminal.flush()
  }

  protected def testKey(): Unit = {
    terminal.writer.write("Input the key event(Enter to complete): ")
    terminal.writer.flush()
    val sb = new StringBuilder
    var more = true
    while (true) {
      val c: Int = reader.asInstanceOf[LineReaderImpl].readCharacter
      if (c == 10 || c == 13) more = false
      else sb.append(new String(Character.toChars(c)))
    }
    terminal.writer().println(KeyMap.display(sb.toString))
    terminal.writer().flush()
  }

  protected def tput(parsedLine: ParsedLine): Unit = {
    if (parsedLine.words.size == 2) {
      val capability: Capability = Capability.byName(parsedLine.words.get(1))
      if (capability != null)
        terminal.puts(capability)
      else
        terminal.writer.println("Unknown capability")
    }
    ()
  }
}

object Main extends App {
  new CliLoop().loop()
}
