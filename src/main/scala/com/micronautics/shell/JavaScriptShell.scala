package com.micronautics.shell

import com.micronautics.cli.MainLoop.terminal
import com.micronautics.cli.{CNodes, MainLoop, Shell}
import com.micronautics.terminal.TerminalStyles.printRichInfo

object JavaScriptShell {
  lazy val cNodes: CNodes = CNodes.empty
}

class JavaScriptShell extends Shell(
  prompt = "javascript",
  cNodes = CNodes.empty,
  evaluator = MainLoop.javaScriptEvaluator
) {
  def input(line: String): Unit = evaluator.eval(line).foreach(x => printRichInfo(s"$x\n"))

  def topHelpMessage: String = s"${ MainLoop.javaScriptEvaluator.info }${ evaluator.status }"
}
