package com.micronautics.cli

object ShellManager {
  lazy val instance = new ShellManager

  def shellStack: ShellStack = instance.shellStack
}

class ShellManager {
  lazy val shellStack: ShellStack = ShellStack.empty

  def topShell: Shell = shellStack.top
}
