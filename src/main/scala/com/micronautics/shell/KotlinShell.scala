package com.micronautics.shell

import com.micronautics.cli.MainLoop.terminal
import com.micronautics.cli.{CNodes, MainLoop, Shell}
import com.micronautics.terminal.TerminalStyles.printRichInfo

class KotlinShell extends Shell(
  prompt = "kotlin",
  cNodes = CNodes.empty,
  evaluator = MainLoop.kotlinEvaluator
) {
  def input(line: String): Unit = evaluator.eval(line).foreach(x => printRichInfo(s"$x\n"))

  def topHelpMessage: String = s"${ MainLoop.kotlinEvaluator.info }${ evaluator.status }"
}
