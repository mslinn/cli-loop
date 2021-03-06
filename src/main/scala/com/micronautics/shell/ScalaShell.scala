package com.micronautics.shell

import com.micronautics.cli.MainLoop.terminal
import com.micronautics.cli.{CNodes, MainLoop, Shell}
import com.micronautics.terminal.TerminalStyles.printRichInfo

class ScalaShell extends Shell(
  prompt = "scala",
  cNodes = CNodes.empty,
  evaluator = MainLoop.scalaEvaluator
) {
  def input(line: String): Unit = evaluator.eval(line).foreach(x => printRichInfo(s"$x\n"))

  def topHelpMessage: String = s"${ MainLoop.scalaEvaluator.info }${ evaluator.status }"
}
