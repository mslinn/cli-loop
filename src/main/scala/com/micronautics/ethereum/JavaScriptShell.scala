package com.micronautics.ethereum

import com.micronautics.cli.MainLoop.{mainLoop, terminal}
import com.micronautics.terminal.TerminalStyles.printRichInfo
import com.micronautics.cli.{CNodes, MainLoop, Shell}

object JavaScriptShell extends Completers {
  lazy val cNodes: CNodes = CNodes()
}

class JavaScriptShell extends Shell(
  prompt = "javascript",
  cNodes = CNodes(),
  evaluator = MainLoop.jsEvaluator,
  topHelpMessage = "Top help message for JavaScript shell"
) {
  def input(line: String): Unit = {
    evaluator.eval(line) match {
      case "help" | "?" | "" => // todo move this check to the main loop
        terminal.writer.println(s"\n$topHelpMessage")
        mainLoop.help(true)

      case result => printRichInfo(result.toString)
    }
  }
}
