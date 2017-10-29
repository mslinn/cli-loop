package com.micronautics.cli

import org.jline.reader.impl.DefaultParser
import org.jline.terminal.{Terminal, TerminalBuilder}


trait TerminalShell extends TerminalStyles with ShellLike {
  implicit lazy val terminal: Terminal =
    TerminalBuilder.builder
      .system(true)
      .build

  implicit lazy val parser: DefaultParser = new DefaultParser
  parser.setEofOnUnclosedQuote(true)
}
