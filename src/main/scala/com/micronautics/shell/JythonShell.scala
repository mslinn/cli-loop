package com.micronautics.shell

import com.micronautics.cli.MainLoop.terminal
import com.micronautics.cli.{CNodes, MainLoop, Shell}
import com.micronautics.terminal.TerminalStyles.printRichInfo

class JythonShell extends Shell(
  prompt = "jython",
  cNodes = CNodes.empty,
  evaluator = MainLoop.jythonEvaluator
) {
  def input(line: String): Unit = evaluator.eval(line).foreach(x => printRichInfo(s"$x\n"))

  def topHelpMessage: String = s"${ MainLoop.jythonEvaluator.info }${ evaluator.status }"
}
