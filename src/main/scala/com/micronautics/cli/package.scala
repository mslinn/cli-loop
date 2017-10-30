package com.micronautics

/** ==Overview==
  * A [[com.micronautics.cli.Shell]] contains properties that define shell instances, for example the prompt,
  * [[org.jline.reader.Completer]] and [[org.jline.reader.Parser]] for the shell's grammar,
  * input evaluator, help message, maximum command verb length, and a Map of function name to function for each command.
  *
  * There is a global [[org.jline.terminal.Terminal]] somewhere.
  *
  * The [[com.micronautics.cli.ShellManager]] singleton is responsible for managing the shells by manipulating the [[com.micronautics.cli.ShellStack]] singleton.
  * [[com.micronautics.cli.ShellManager]] considers the `Shell` on the top of the stack to be active,
  * and it forwards user input to the currently active [[com.micronautics.cli.Shell]].
  * When the [[com.micronautics.cli.ShellStack]] is empty, [[com.micronautics.cli.ShellManager]] terminates the program. */
package object cli
