package com.micronautics.cli

import org.jline.terminal.Terminal
import org.jline.utils.{AttributedStringBuilder, AttributedStyle}

trait TerminalStyles {
  val defaultStyle: AttributedStyle = AttributedStyle.DEFAULT
  val errorStyle: AttributedStyle   = AttributedStyle.DEFAULT.foreground(AttributedStyle.RED)
  val helpStyle: AttributedStyle    = AttributedStyle.DEFAULT.foreground(AttributedStyle.CYAN)
  val infoStyle: AttributedStyle    = AttributedStyle.DEFAULT.foreground(AttributedStyle.MAGENTA)
  val jsStyle: AttributedStyle      = AttributedStyle.DEFAULT.foreground(AttributedStyle.CYAN)

  @inline def printRichError(message: String)
                            (implicit terminal: Terminal): Unit =
    terminal.writer.println(richError(message))

  @inline def printRichInfo(message: String)
                           (implicit terminal: Terminal): Unit =
    terminal.writer.println(info(message))

  @inline def printRichHelp(message: String)
                           (implicit terminal: Terminal): Unit =
    terminal.writer.println(help(message))

  @inline def printJsResult(message: String)
                           (implicit terminal: Terminal): Unit =
    terminal.writer.println(js(message))

  def richError(message: String): String = {
    val asBuilder = new AttributedStringBuilder
    if (!TerminalCapabilities.supportsAnsi) asBuilder.append(s" Error: $message ")
    else asBuilder
           .style(errorStyle)
           .append(s" Error: $message ")
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