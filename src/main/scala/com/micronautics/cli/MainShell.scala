package com.micronautics.cli

import com.micronautics.ethereum._
import org.jline.reader.{EndOfFileException, LineReader, LineReaderBuilder, UserInterruptException}
import org.jline.utils.{AttributedStringBuilder, AttributedStyle}

object MainShell extends ShellCommands

abstract class MainShell(promptName: String) extends TerminalShell {

  protected val reader: LineReader = LineReaderBuilder.builder
    .terminal(terminal)
    .completer(CommandShell.completer)
    .parser(parser)
    .build
  // If the user's first keypress is a tab, all of the top-level node values are displayed, thereby displaying the available commands
  reader.unsetOpt(LineReader.Option.INSERT_TAB)

  protected lazy val javaScript: JavaScript = new JavaScript().setup()


  def run(): Unit = {
    help()
    signInMessage()
    loop()
  }


  protected def fullHelp(): Unit

  protected def processCommandLine(line: String): Unit

  protected def processJavaScriptLine(line: String): AnyRef

  def signInMessage(): Unit


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
            mode match {
              case EthereumMode.COMMAND =>
                printRichInfo("\nExiting program.")
                System.exit(0)

              case EthereumMode.JAVASCRIPT =>
                printRichInfo("Returning to command mode.\n")
                help()
                mode = EthereumMode.COMMAND
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
