package com.micronautics.cli

import com.micronautics.evaluator.Evaluator

/** Contains properties that define shell instances, for example the prompt, help message,
  * maximum command verb length, and a Map of function name to function for each command */
case class Shell(
  prompt: String,
  commandNodes: CNodes,
  evaluator: Evaluator,
  topHelpMessage: String = ""
) {
  lazy val commandFunctions: Map[String, Any => Any] =
    commandNodes.cNodes.map {
      case CNode(name, function, _, _, _) => (name, function)
    }.toMap

  def functionFor(name: String): Option[Any => Any] = commandFunctions.get(name)

  lazy val commandHelps: Map[String, String] =
    commandNodes
      .cNodes
      .sortBy(_.name)
      .map { case CNode(name, _, helpMessage, _, _) => (name, helpMessage) }
      .toMap

  def helpFor(name: String): Option[String] = commandHelps.get(name)

  lazy val completeHelpMessage: String = topHelpMessage + commandHelps.map { help => s"${ help._1 } - ${ help._2 }"  }
}
