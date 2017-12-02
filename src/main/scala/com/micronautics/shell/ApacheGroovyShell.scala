package com.micronautics.shell

import com.micronautics.cli.MainLoop.terminal
import com.micronautics.cli.{CNodes, MainLoop, Shell}
import com.micronautics.terminal.TerminalStyles.printRichInfo

class ApacheGroovyShell extends Shell(
  prompt = "groovy",
  cNodes = CNodes.empty,
  evaluator = MainLoop.groovyEvaluator
) {
  def input(line: String): Unit = evaluator.eval(line).foreach(x => printRichInfo(s"$x\n"))

  def topHelpMessage: String = s"${ MainLoop.groovyEvaluator.info }${ evaluator.status }"
}
