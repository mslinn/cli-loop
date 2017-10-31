package com.micronautics.terminal

import org.jline.terminal.Terminal
import org.jline.utils.{AttributedStringBuilder, AttributedStyle}

object TerminalStyles {
  val defaultStyle: AttributedStyle   = AttributedStyle.DEFAULT
  val debugStyle: AttributedStyle     = defaultStyle.foreground(AttributedStyle.YELLOW)
  val errorStyle: AttributedStyle     = defaultStyle.foreground(AttributedStyle.RED)
  val helpStyle: AttributedStyle      = defaultStyle.foreground(AttributedStyle.CYAN)
  val helpCNameStyle: AttributedStyle = helpStyle.bold
  val infoStyle: AttributedStyle      = defaultStyle.foreground(AttributedStyle.MAGENTA)
  val jsStyle: AttributedStyle        = defaultStyle.foreground(AttributedStyle.CYAN)

  @inline def printRichDebug(message: String)
                              (implicit terminal: Terminal): Unit = {
      terminal.writer.println(richDebug(message))
      terminal.writer.flush()
    }

  @inline def printRichError(message: String)
                            (implicit terminal: Terminal): Unit = {
    terminal.writer.println(richError(message))
    terminal.writer.flush()
  }

  @inline def printRichInfo(message: String)
                           (implicit terminal: Terminal): Unit = {
    terminal.writer.println(info(message))
    terminal.writer.flush()
  }

  @inline def printRichHelp(message: String)
                           (implicit terminal: Terminal): Unit = {
    terminal.writer.println(help(message))
    terminal.writer.flush()
  }

  @inline def printJsResult(message: String)
                           (implicit terminal: Terminal): Unit = {
    terminal.writer.println(js(message))
    terminal.writer.flush()
  }

  def richDebug(message: String): String = {
    val asBuilder = new AttributedStringBuilder
    if (!TerminalCapabilities.supportsAnsi) asBuilder.append(message)
    else asBuilder
           .style(debugStyle)
           .append(message)
           .style(defaultStyle)
  }.toAnsi

  def richError(message: String): String = {
    val asBuilder = new AttributedStringBuilder
    if (!TerminalCapabilities.supportsAnsi) asBuilder.append(s"Error: $message\n")
    else asBuilder
           .style(errorStyle)
           .append(s"Error: $message\n")
           .style(defaultStyle)
  }.toAnsi

  def help(message: String): String = {
    val asBuilder = new AttributedStringBuilder
    if (!TerminalCapabilities.supportsAnsi) asBuilder.append(message)
    else asBuilder
           .style(helpStyle)
           .append(message)
           .style(defaultStyle)
  }.toAnsi

  def info(message: String): String = {
    val asBuilder = new AttributedStringBuilder
    if (!TerminalCapabilities.supportsAnsi) asBuilder.append(message)
        else asBuilder
              .style(infoStyle)
              .append(message)
              .style(defaultStyle)
  }.toAnsi

  def js(message: String): String = {
    val asBuilder = new AttributedStringBuilder
    if (!TerminalCapabilities.supportsAnsi) asBuilder.append(message)
        else asBuilder
              .style(jsStyle)
              .append(message)
              .style(defaultStyle)
  }.toAnsi
}
