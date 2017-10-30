package com.micronautics.cli

import com.micronautics.ethereum._
import com.micronautics.evaluator.{EthereumEvaluator, JavaScriptEvaluator}
import org.jline.reader.{EndOfFileException, LineReader, LineReaderBuilder, Parser, UserInterruptException}
import org.jline.terminal.{Terminal, TerminalBuilder}
import org.jline.utils.{AttributedStringBuilder, AttributedStyle}
import TerminalStyles._
import org.jline.reader.impl.DefaultParser

object MainLoop {
  implicit lazy val parser: DefaultParser = new DefaultParser
  parser.setEofOnUnclosedQuote(true)

  implicit lazy val terminal: Terminal =
    TerminalBuilder.builder
      .system(true)
      .build

  lazy val mainLoop: MainLoop = new MainLoop

  lazy val ethereumEvaluator: EthereumEvaluator = new EthereumEvaluator().setup()
  lazy val jsEvaluator: JavaScriptEvaluator = new JavaScriptEvaluator().setup()

  lazy val ethereumShell: EthereumShell = new EthereumShell()
  lazy val jsShell: EthereumShell = new JavaScriptShell()

  private var _activeShell: Shell = ethereumShell

  def activeShell: Shell = _activeShell

  def activeShell_= (newValue: Shell): Unit = {
    _activeShell = newValue
    mainLoop.reader = reader(parser, terminal)
  }

  // todo should this be cached, and recomputed whenever `activeShell` changes?
  protected def reader(parser: Parser, terminal: Terminal): LineReader = {
    val x = LineReaderBuilder.builder
      .terminal(terminal)
      .completer(activeShell.cNodes.completer)
      .parser(parser)
      .build

    // If the user's first keypress is a tab, all of the top-level node values are displayed, thereby displaying the available commands
    x.unsetOpt(LineReader.Option.INSERT_TAB)
    x
  }
}

class MainLoop extends ShellLike {
  import MainLoop._

  var reader: LineReader = MainLoop.reader(parser, terminal)

  def run(): Unit = {
    help()
    signInMessage()
    loop()
  }


  protected def fullHelp(): Unit

  protected def processCommandLine(line: String): Unit

  protected def processJavaScriptLine(line: String): AnyRef

  protected def signInMessage(): Unit


  // todo add parameters for helpCmd name/value tuples/triples
  protected def help(full: Boolean = false): Unit = {
    /** This implicit acts as a local accumulator for the rich help message */
    implicit val asb: AttributedStringBuilder = {
      val asBuilder = new AttributedStringBuilder
      if (!TerminalCapabilities.supportsAnsi) asBuilder.append("Commands are: ")
      else asBuilder
             .style(helpStyle)
             .append("Commands are: ")
    }

    commands.zipWithIndex.foreach { case (x, i) =>
      x match {
        case name: String if i==commands.length-1 => bold(name, isLast=true)
        case name: String if i==commands.length-2 => bold(name, isPenultimate=true)
        case name: String => bold(name)
        case (name: String, alias: String) => bold(name, alias)
      }
    }

    if (TerminalCapabilities.supportsAnsi) asb.style(defaultStyle)
    terminal.writer.println(asb.toAnsi)

    if (full) fullHelp()
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
            val (nextShell, shellStack) = ShellManager.shellStack.pop()
            if (shellStack.isEmpty) {
                printRichInfo("\nExiting program.")
                System.exit(0)
            } else {
              activeShell = nextShell
              printRichInfo("Returning to command mode.\n")
              help()
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
        else if (mode==EthereumMode.COMMAND)
          processCommandLine(line)
        else if (mode==EthereumMode.JAVASCRIPT)
          processJavaScriptLine(line)
      }
    }
  }

  protected def prompt: String = {
    val asBuilder = new AttributedStringBuilder
    if (!TerminalCapabilities.supportsAnsi)
      asBuilder.append(s"promptName [$gitBranch] ${ mode.name.toLowerCase }> ")
    else asBuilder
           .style(AttributedStyle.DEFAULT.foreground(AttributedStyle.GREEN))
           .append(promptName)
           .style(AttributedStyle.DEFAULT.foreground(AttributedStyle.YELLOW))
           .append(s" [$gitBranch]")
           .style(AttributedStyle.DEFAULT.foreground(AttributedStyle.GREEN))
           .append(s" ${ mode.name.toLowerCase }")
           .style(AttributedStyle.DEFAULT.foreground(AttributedStyle.MAGENTA))
           .append("> ")
           .style(AttributedStyle.DEFAULT.foregroundDefault)
  }.toAnsi
}
