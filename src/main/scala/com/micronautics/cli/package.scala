package com.micronautics

/** ==Overview==
  * A [[com.micronautics.cli.Shell]] contains properties that define shell instances, for example the prompt,
  * input evaluator, help message, maximum command verb length, and a Map of function name to function for each command.
  * The [[com.micronautics.cli.ShellGlobal]] singleton contains properties that are common to all shells, for example the
  * [[org.jline.terminal.Terminal]], [[org.jline.reader.Parser]] and a [[org.jline.reader.Completer]] for the entire application grammar.
  *
  * The [[com.micronautics.cli.ShellManager]] singleton is responsible for managing the shells by manipulating the [[com.micronautics.cli.ShellStack]] singleton.
  * [[com.micronautics.cli.ShellManager]] considers the `Shell` on the top of the stack to be active,
  * and it forwards user input to the currently active [[com.micronautics.cli.Shell]].
  * When the [[com.micronautics.cli.ShellStack]] is empty, [[com.micronautics.cli.ShellManager]] terminates the program.
  *
  * Shell*/
package object cli
