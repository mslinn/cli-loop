package com.micronautics.ethereum

import com.micronautics.cli.MainLoop.{mainLoop, terminal}
import com.micronautics.terminal.TerminalStyles.{ printRichDebug, printRichInfo }
import com.micronautics.cli.{CNodes, MainLoop, Shell}

object JavaScriptShell {
  lazy val cNodes: CNodes = CNodes.empty
}

class JavaScriptShell extends Shell(
  prompt = "javascript",
  cNodes = CNodes.empty,
  evaluator = MainLoop.jsEvaluator,
  topHelpMessage = "Top help message for JavaScript shell"
) {
  def input(line: String): Unit = {
    line match {
      case _ => printRichInfo(evaluator.eval(line).toString)
    }
  }
}
