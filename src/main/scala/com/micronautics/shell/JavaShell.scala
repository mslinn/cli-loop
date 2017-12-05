package com.micronautics.shell

import com.micronautics.cli.MainLoop.terminal
import com.micronautics.cli.{CNodes, MainLoop, Shell}
import com.micronautics.terminal.TerminalStyles.printRichInfo

class JavaShell extends Shell(
  prompt = "java",
  cNodes = CNodes.empty,
  evaluator = MainLoop.jrubyEvaluator
) {
  def input(line: String): Unit = evaluator.eval(line).foreach(x => printRichInfo(s"$x\n"))

  def topHelpMessage: String = s"${ MainLoop.javaEvaluator.info }${ evaluator.status }"
}
