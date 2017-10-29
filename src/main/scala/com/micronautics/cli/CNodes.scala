package com.micronautics.cli

import com.micronautics.cli.TerminalStyles._
import org.jline.builtins.Completers.TreeCompleter
import org.jline.builtins.Completers.TreeCompleter.{Node, node}
import org.jline.reader.impl.completer.{AggregateCompleter, ArgumentCompleter}
import org.jline.utils.AttributedStringBuilder

object CNode {
  protected val nullFunction: Any => Any = () => {}
}

case class CNode(
  name: String,
  function: Any => Any = CNode.nullFunction,
  helpMessage: String = "",
  children: CNodes = CNodes(),
  alias: String = ""
) {
  lazy val paddedChildNames: List[String] = children.paddedNames

  lazy val richHelp: String = if (TerminalCapabilities.supportsAnsi) {
    val asb: AttributedStringBuilder =
      new AttributedStringBuilder()
        .append("\n")
        .style(helpStyle)
        .append(helpMessage)
        .style(defaultStyle)
    asb.toAnsi
  } else helpMessage

  lazy val width: Int = name.length

  def paddedName(width: Int): String = name + " "*(width - name.length)
}

/** Wraps a collection of [[CNode]] */
case class CNodes(cNodes: CNode*) {
  lazy val isEmpty: Boolean = cNodes.isEmpty

  lazy val maxWidth: Int = cNodes.map(_.width).max

  lazy val nodes: List[Node] = convertToNodes(cNodes.toList)

  lazy val nonEmpty: Boolean = cNodes.nonEmpty

  lazy val sortedNodes: List[CNode] = cNodes.toList.sortBy(_.name)

  /** Useful for help messages? Delete? */
  lazy val paddedNames: List[String] =
    sortedNodes.map(node => node.paddedName(maxWidth))

  lazy val helpMessages: String =
    sortedNodes.map { cNode => helpMessage(cNode.name) }.mkString("\n")

  protected lazy val treeCompleter: TreeCompleter = new TreeCompleter(nodes: _*)

  // TODO unsure how to define
  protected lazy val argumentCompleter: ArgumentCompleter = new ArgumentCompleter()

  // TODO unsure how to get argumentCompleter to do something useful
  lazy val completer: AggregateCompleter = new AggregateCompleter(
    new ArgumentCompleter(treeCompleter, argumentCompleter)
  )


  def helpMessage(name: String): Option[String] =
    for {
      paddedName <- paddedName(name)
      node       <- cNodeFor(name)
    } yield {
      if (TerminalCapabilities.supportsAnsi) {
        new AttributedStringBuilder().append("\n")
          .style(helpCNameStyle)
          .append(paddedName)
          .style(defaultStyle)
          .append(" - ")
          .style(helpStyle)
          .append(node.helpMessage)
          .style(defaultStyle)
          .toAnsi
      } else s"$paddedName - ${ node.helpMessage }"
    }

  /** Useful for help messages */
  def paddedName(name: String): Option[String] =
    cNodes
      .find(_.name==name)
      .map(node => node.paddedName(maxWidth))


  protected def cNodeFor(name: String): Option[CNode] = cNodes.find(_.name==name)

  protected def convertToNodes(cNodes: List[CNode]): List[Node] =
    cNodes.map { cNode =>
      if (cNode.children.isEmpty) node(cNode.name)
      else {
        val childNodes: Seq[Node] = convertToNodes(cNode.children.cNodes.toList)
        node(cNode.name, childNodes: _*)
      }
    }
}
