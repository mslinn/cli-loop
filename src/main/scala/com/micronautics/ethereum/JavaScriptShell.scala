package com.micronautics.ethereum

import com.micronautics.cli.MainLoop.terminal
import com.micronautics.cli.{CNodes, MainLoop, Shell}
import com.micronautics.terminal.TerminalStyles.printRichInfo

object JavaScriptShell {
  lazy val cNodes: CNodes = CNodes.empty
}

class JavaScriptShell extends Shell(
  prompt = "javascript",
  cNodes = CNodes.empty,
  evaluator = MainLoop.jsEvaluator,
  topHelpMessage = "Top help message for JavaScript shell"
) {
  def input(line: String): Unit = printRichInfo(s"${ evaluator.eval(line)}\n")
}
