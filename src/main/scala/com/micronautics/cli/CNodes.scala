package com.micronautics.cli

import com.micronautics.terminal.TerminalCapabilities
import com.micronautics.terminal.TerminalStyles.{defaultStyle, helpCNameStyle, helpStyle}
import org.jline.builtins.Completers.TreeCompleter
import org.jline.builtins.Completers.TreeCompleter.{Node, node}
import org.jline.reader.impl.completer.{AggregateCompleter, ArgumentCompleter}
import org.jline.utils.AttributedStringBuilder

/** @see http://stackoverflow.com/a/3508555/553865 */
sealed trait StringOrTuple[-T]

object StringOrTuple {
  implicit object StringWitness extends StringOrTuple[String]
  implicit object TupleWitness extends StringOrTuple[(String, String)]
}

/** Wraps a collection of [[CNode]] */
case class CNodes(cNodes: CNode*) {
  lazy val aliases: List[String] = sortedNodes.map(_.alias)

  lazy val commandNames: List[String] = sortedNodes.map(_.name)

  /** @return List("name1", ("name2", "alias"), "name3") */
  def commandAliasNames[T: StringOrTuple]: List[T] = sortedNodes.map {
    case node if node.alias.isEmpty => node.name.asInstanceOf[T]
    case node                       => (node.name, node.alias).asInstanceOf[T]
  }

  lazy val isEmpty: Boolean = cNodes.isEmpty

  lazy val maxWidth: Int = cNodes.map(_.width).max

  lazy val nodes: List[Node] = convertToNodes(cNodes.toList)

  lazy val nonEmpty: Boolean = cNodes.nonEmpty

  lazy val sortedNodes: List[CNode] = cNodes.toList.sortBy(_.name)

  /** Useful for help messages? Delete? */
  lazy val paddedCommandNames: List[String] =
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
        node(cNode.name, childNodes)
      }
    }
}
