package com.micronautics.cli

import com.micronautics.terminal.TerminalCapabilities
import com.micronautics.terminal.TerminalStyles._
import org.jline.utils.AttributedStringBuilder

object CNode {
  protected val nullFunction: Any => Any = (_: Any) => null.asInstanceOf[Any]
}

case class CNode(
  name: String,
  function: Any => Any = CNode.nullFunction,
  helpMessage: String = "",
  children: CNodes = CNodes(),
  alias: String = ""
) {
  lazy val paddedChildNames: List[String] = children.paddedCommandNames

  lazy val richHelp: String = if (TerminalCapabilities.supportsAnsi) {
    val asb: AttributedStringBuilder =
      new AttributedStringBuilder()
        .append("\n")
        .style(helpStyle)
        .append(helpMessage)
        .style(defaultStyle)
    asb.toAnsi
  } else helpMessage

  lazy val width: Int = math.max(name.length, alias.length)

  def paddedName(width: Int): String = this match {
    case _ if alias.isEmpty => name + " "*(width - name.length)
    case _                  => s"$name / $alias" + " "*(width - name.length - "/".length - alias.length)
  }
}
