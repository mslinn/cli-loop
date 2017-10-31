package com.micronautics.cli

import com.micronautics.evaluator.Evaluator
import org.jline.builtins.Completers.TreeCompleter.Node

/** Contains properties that define shell instances, for example the prompt, help message,
  * maximum command verb length, and a Map of function name to function for each command */
case class Shell(
  prompt: String,
  cNodes: CNodes,
  evaluator: Evaluator,
  topHelpMessage: String = ""
) {
  lazy val nodes: List[Node] = cNodes.nodes

  def functionFor(nameOrAlias: String): Option[Any => Any] = cNodes.commandFunctions.get(nameOrAlias)

  def helpFor(name: String): Option[String] = cNodes.commandHelps.get(name)

  lazy val completeHelpMessage: String = s"$topHelpMessage\n" + cNodes.completeHelpMessage
}
