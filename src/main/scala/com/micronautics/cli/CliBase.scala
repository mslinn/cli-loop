package com.micronautics.cli

import org.eclipse.jgit.lib.Repository
import org.eclipse.jgit.storage.file.FileRepositoryBuilder
import org.jline.reader.impl.DefaultParser
import org.jline.terminal.{Terminal, TerminalBuilder}
import org.jline.utils.{AttributedStringBuilder, AttributedStyle}

trait CliBase {
  lazy val parser: DefaultParser = new DefaultParser
  parser.setEofOnUnclosedQuote(true)

  val commands: List[Any]

  protected lazy val cmdMaxWidth: Int = commands.map {
    case string: String => string.length
    case (name: String, alias: String) => name.length + alias.length + 1
  }.max

  val defaultStyle: AttributedStyle = AttributedStyle.DEFAULT
  val errorStyle: AttributedStyle   = AttributedStyle.DEFAULT.foreground(AttributedStyle.RED)
  val helpStyle: AttributedStyle    = AttributedStyle.DEFAULT.foreground(AttributedStyle.CYAN)
  val infoStyle: AttributedStyle    = AttributedStyle.DEFAULT.foreground(AttributedStyle.MAGENTA)

  lazy val terminal: Terminal =
    TerminalBuilder.builder
      .system(true)
      .build

  // todo control whether rich text is output or not based on the value of useColor
  def bold(name: String, isPenultimate: Boolean = false, isLast: Boolean = false)
                    (implicit asBuilder: AttributedStringBuilder): AttributedStringBuilder =
    if (!TerminalCapabilities.supportsAnsi) {
      asBuilder.append(name)
      if (isPenultimate)
        asBuilder.append(" and ")
      else if (!isLast)
        asBuilder.append(", ")
      asBuilder
    } else {
      asBuilder
        .style(AttributedStyle.DEFAULT.foreground(AttributedStyle.YELLOW))
        .append(name)
        .style(AttributedStyle.DEFAULT.foregroundDefault)
        .style(AttributedStyle.DEFAULT.faint)

      if (isPenultimate)
        asBuilder.append(" and ")
      else if (!isLast)
        asBuilder.append(", ")

      asBuilder.style(AttributedStyle.DEFAULT.faintDefault)
    }

  // todo control whether rich text is output or not based on the value of useColor
  def bold(name: String, alias: String)
                    (implicit asBuilder: AttributedStringBuilder): AttributedStringBuilder = {
    if (!TerminalCapabilities.supportsAnsi) asBuilder.append(s"$name / $alias, ")
    else asBuilder
      .style(AttributedStyle.DEFAULT.foreground(AttributedStyle.YELLOW))
      .append(name)
      .style(AttributedStyle.DEFAULT.faint)
      .append("/")
      .style(AttributedStyle.DEFAULT.faintDefault)
      .style(AttributedStyle.DEFAULT.foreground(AttributedStyle.GREEN))
      .append(alias)
      .style(AttributedStyle.DEFAULT.foregroundDefault)
      .style(AttributedStyle.DEFAULT.faint)
      .append(", ")
      .style(AttributedStyle.DEFAULT.faintDefault)
  }

  @inline def printRichError(message: String): Unit = terminal.writer.println(richError(message))

  @inline def printRichInfo(message: String): Unit = terminal.writer.println(info(message))

  @inline def printRichHelp(message: String): Unit = terminal.writer.println(help(message))

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

  protected def gitRepo: Repository = FileRepositoryBuilder.create(new java.io.File(".git/").getAbsoluteFile)

  def gitBranch: String = gitRepo.getBranch

  def helpCmd(name: String, message: String)
             (implicit asBuilder: AttributedStringBuilder): AttributedStringBuilder =
    if (!TerminalCapabilities.supportsAnsi) asBuilder.append(name + " "*(cmdMaxWidth-name.length) + s" - $message\n")
    else asBuilder
           .style(AttributedStyle.DEFAULT.bold)
           .append(name + " "*(cmdMaxWidth-name.length))
           .style(AttributedStyle.DEFAULT.boldOff)
           .append(s" - $message\n")

  def helpCmd(name: String, alias: String, message: String)
             (implicit asBuilder: AttributedStringBuilder): AttributedStringBuilder =
    if (!TerminalCapabilities.supportsAnsi)
      asBuilder.append(s"$name/$alias" + " "*(cmdMaxWidth - name.length - alias.length - 1) + s" - $message\n")
    else asBuilder
           .style(AttributedStyle.DEFAULT.bold)
           .append(name)
           .style(AttributedStyle.DEFAULT.boldOff)
           .append("/")
           .style(AttributedStyle.DEFAULT.bold)
           .append(alias)
           .style(AttributedStyle.DEFAULT.boldOff)
           .append(" "*(cmdMaxWidth - name.length - alias.length - 1) + s" - $message\n")
}
