package com.micronautics.ethereum

import java.util.{Map => JMap}
import com.micronautics.Main
import com.micronautics.cli._
import org.jline.keymap.KeyMap
import org.jline.reader.impl.LineReaderImpl
import org.jline.reader.{Binding, LineReader, Macro, ParsedLine, Reference}
import org.jline.utils.InfoCmp.Capability
import scala.collection.JavaConverters._

trait ShellCommands extends ShellLike {
}

object EthereumShell extends Completers {
  lazy val cNodes: CNodes =
    CNodes(
      CNode("account",    helpMessage="Ethereum account management"),
      CNode("bindkey",    helpMessage="Show all key bindings"),
      CNode("exit",       helpMessage="Display this message", alias="^d"),
      CNode("javascript", helpMessage="Enter JavaScriptEvaluator console"),
      CNode("help",       alias="?"),
      CNode("password",   helpMessage="Set the password"),
      CNode("set",        helpMessage=s"Set a terminal variable, such as '${ LineReader.PREFER_VISIBLE_BELL }'"),
      CNode("testkey",    helpMessage="Test a key binding"),
      CNode("tput",       helpMessage="Demonstrate a terminal capability, such as 'bell'")
    )
}

class EthereumShell extends Shell(
  prompt = "beth",
  cNodes = CNodes(),
  evaluator = MainLoop.ethereumEvaluator,
  topHelpMessage = "Top help message for Ethereum shell"
) {
  import com.micronautics.terminal.TerminalStyles._
  import MainLoop._

  def input(line: String): Unit = {
    val parsedLine: ParsedLine = mainLoop.reader.getParser.parse(line, 0)
    parsedLine.word match {
      case "account" => account(parsedLine)

      case "bindkey" => bindKey(parsedLine)

      case "javascript" =>
        Main.shellManager.shellStack.push(jsShell)
        printRichInfo("Entering JavaScriptEvaluator mode. Press Control-d to return to command mode.\n")

      case "help" | "?" | "" => // todo move this check to the main loop
        terminal.writer.println(s"\n$topHelpMessage")
        mainLoop.help(true)

      case "set" => set(parsedLine)

      case "testkey" => testKey()

      case "tput" => tput(parsedLine)

      case x =>
        printRichError(s"'$x' is an unknown command.") // todo show entire help
    }
  }

  def signInMessage(): Unit = printRichHelp("Press <tab> multiple times for tab completion of commands and options.\n")

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
      val bound: JMap[String, Binding] = mainLoop.reader.getKeys.getBoundKeys
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
      mainLoop.reader.getKeys.bind(
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
        mainLoop.reader.setVariable(parsedLine.words.get(0), parsedLine.words.get(1))

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
      val c: Int = mainLoop.reader.asInstanceOf[LineReaderImpl].readCharacter
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
