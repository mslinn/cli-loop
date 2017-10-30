package com.micronautics.cli

import com.micronautics.evaluator.Evaluator

/** Contains properties that define shell instances, for example the prompt, help message,
  * maximum command verb length, and a Map of function name to function for each command */
case class Shell(
  prompt: String,
  cNodes: CNodes,
  evaluator: Evaluator,
  topHelpMessage: String = ""
) {
  /** Maps names and aliases to functions */
  lazy val commandFunctions: Map[String, Any => Any] = {
    val nameToFunction = cNodes.cNodes.map {
      case CNode(name, function, _, _, _) => (name, function)
    }

    val aliasToFunction = cNodes.cNodes.map {
      case CNode(_, function, _, _, alias) if alias.trim.nonEmpty => (alias, function)
    }

    (nameToFunction ++ aliasToFunction).toMap
  }

  lazy val completeHelpMessage: String = {
    val widest = commandHelps.map(_._1.length).max
    topHelpMessage + "\n" + commandHelps.map { help =>
      val paddedName = help._1 + " "*(widest - help._1.length)
      s"$paddedName - ${ help._2 }"
    }.mkString("\n")
  }

  def functionFor(nameOrAlias: String): Option[Any => Any] = commandFunctions.get(nameOrAlias)

  def helpFor(name: String): Option[String] = commandHelps.get(name)

  protected lazy val commandHelps: Map[String, String] =
    cNodes
      .cNodes
      .sortBy(_.name)
      .map { case CNode(name, _, helpMessage, _, alias) =>
        val key = if (alias.nonEmpty) s"$name/$alias" else name
        (key, helpMessage)
      }
      .toMap
}
