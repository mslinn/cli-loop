package com.micronautics.cli

import org.eclipse.jgit.lib.Repository
import org.eclipse.jgit.storage.file.FileRepositoryBuilder
import org.jline.utils.{AttributedStringBuilder, AttributedStyle}

trait ShellLike {
  val commands: List[Any]

  protected lazy val cmdMaxWidth: Int = commands.map {
    case string: String => string.length
    case (name: String, alias: String) => name.length + alias.length + 1
  }.max

  protected def gitRepo: Repository = FileRepositoryBuilder.create(new java.io.File(".git/").getAbsoluteFile)

  def gitBranch: String = gitRepo.getBranch

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
