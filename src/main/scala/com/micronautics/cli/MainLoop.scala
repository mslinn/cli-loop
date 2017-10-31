package com.micronautics.cli

import com.micronautics.terminal.TerminalStyles._
import com.micronautics.ethereum._
import com.micronautics.evaluator.{EthereumEvaluator, Evaluator, JavaScriptEvaluator}
import com.micronautics.terminal.TerminalCapabilities
import org.eclipse.jgit.lib.Repository
import org.eclipse.jgit.storage.file.FileRepositoryBuilder
import org.jline.reader.impl.DefaultParser
import org.jline.reader.{EndOfFileException, LineReader, LineReaderBuilder, Parser, UserInterruptException}
import org.jline.terminal.{Terminal, TerminalBuilder}
import org.jline.utils.{AttributedStringBuilder, AttributedStyle}

object MainLoop {
  implicit lazy val parser: DefaultParser = new DefaultParser
  parser.setEofOnUnclosedQuote(true)

  implicit lazy val terminal: Terminal =
    TerminalBuilder.builder
      .system(true)
      .build

  lazy val ethereumEvaluator: Evaluator = new EthereumEvaluator().setup()
  lazy val ethereumShell: EthereumShell = new EthereumShell

  lazy val jsEvaluator: JavaScriptEvaluator = new JavaScriptEvaluator().setup().asInstanceOf[JavaScriptEvaluator]
  lazy val jsShell: JavaScriptShell = new JavaScriptShell

  lazy val mainLoop: MainLoop = new MainLoop(ethereumShell)

  lazy val shellManager: ShellManager = ShellManager.instance
  shellManager.shellStack.push(ethereumShell)

  def gitBranch: String = try { gitRepo.getBranch } catch { case _: Exception => "" }

  protected def gitRepo: Repository = FileRepositoryBuilder.create(new java.io.File(".git/").getAbsoluteFile)

  // todo should this be cached, and recomputed whenever `activeShell` changes?
  protected def reader(parser: Parser, terminal: Terminal): LineReader = {
    val lineReader: LineReader = LineReaderBuilder.builder
      .terminal(terminal)
      .completer(shellManager.topShell.completer)
      .parser(parser)
      .build

    // If the user's first keypress is a tab, all of the top-level node values are displayed, thereby displaying the available commands
    lineReader.unsetOpt(LineReader.Option.INSERT_TAB)
    lineReader
  }
}

class MainLoop(val shell: Shell) extends ShellLike {
  import com.micronautics.cli.MainLoop._

  def reader: LineReader = MainLoop.reader(parser, terminal)

  val cNodes: CNodes = shell.cNodes

  // List might contain a name: String or (name: String, alias: String)
  def commands: List[Any] = cNodes.commandAliasNames

  val aliases: List[String] = cNodes.aliases


  def run(): Unit = {
    signInMessage()
    help()
    loop()
  }


  protected def signInMessage(): Unit = printRichHelp(shellManager.topShell.topHelpMessage)


  // todo add parameters for helpCmd name/value tuples/triples
  def help(showCompleteHelp: Boolean = false): Unit = {
    /** This implicit acts as a local accumulator for the rich help message */
    implicit val asb: AttributedStringBuilder = {
      val asBuilder = new AttributedStringBuilder
      if (!TerminalCapabilities.supportsAnsi) asBuilder.append("Commands are: ")
      else asBuilder
             .style(helpStyle)
             .append("Commands are: ")
    }

    commands.zipWithIndex.foreach { case (stringOrTuple, i) =>
      stringOrTuple match {
        case name:String if i==commands.length-1 => bold(name, isLast=true)

        case name: String if i==commands.length-2 => bold(name, isPenultimate=true)

        case name: String => bold(name)

        case (name: String, alias: String) => bold(name, alias)
      }
    }

    if (TerminalCapabilities.supportsAnsi) asb.style(defaultStyle)
    terminal.writer.println(asb.toAnsi)

    if (showCompleteHelp) shellManager.topShell.completeHelpMessage
    ()
  }

  /** Effectively dds the following commands to every completer: Control-d, help, ?, quit, exit */
  protected def loop(): Unit = {
    while (shellManager.nonEmpty) {
      val topShell = shellManager.topShell
      val line: String = try { readLine.trim } catch { case _: Exception => "" }
      if (line.nonEmpty) {
        line.toLowerCase match {
          case "help" | "?" | "" =>
            printRichHelp(s"\n${ topShell.completeHelpMessage }\n")
            mainLoop.help(true)

          case "quit" | "exit" =>
            exitShell()

          case _ =>
            topShell.input(line)
          }
        }
      }
    }

  protected def readLine: String =
    try {
      reader.readLine(prompt)
    } catch {
      case _: UserInterruptException =>
        printRichInfo("Press Control-D to exit.")
        ""

      case _: EndOfFileException =>
        exitShell()
        ""

      case e: Exception =>
        printRichError("Error: " + e.getMessage)
        ""
    }

  protected def exitShell(): Unit = {
    val (nextShell, shellStack) = ShellManager.shellStack.pop()
    if (shellStack.isEmpty) {
      exit()
    } else {
      printRichInfo(s"Returning to ${ nextShell.prompt }.\n")
      help()
    }
  }

  protected def exit(): Unit = {
    // todo clean up - one day close the console log
    System.exit(0)
  }

  protected def safeGitBranch: String = {
    val branch = gitBranch
    if (branch.nonEmpty) s" [$gitBranch]" else ""
  }

  protected def prompt: String = {
    import GlobalConfig.instance.productName
    val asBuilder = new AttributedStringBuilder
    val topShell = shellManager.topShell
    if (!TerminalCapabilities.supportsAnsi)
      asBuilder.append(s"$productName$safeGitBranch ${ topShell.prompt }> ")
    else asBuilder
           .style(AttributedStyle.DEFAULT.foreground(AttributedStyle.GREEN))
           .append(productName)
           .style(AttributedStyle.DEFAULT.foreground(AttributedStyle.YELLOW))
           .append(safeGitBranch)
           .style(AttributedStyle.DEFAULT.foreground(AttributedStyle.GREEN))
           .append(s" ${ topShell.prompt.toLowerCase }")
           .style(AttributedStyle.DEFAULT.foreground(AttributedStyle.MAGENTA))
           .append("> ")
           .style(AttributedStyle.DEFAULT.foregroundDefault)
  }.toAnsi
}
