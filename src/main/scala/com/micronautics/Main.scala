package com.micronautics

import com.micronautics.cli.ShellManager
import com.micronautics.ethereum.EthereumShell

object Main extends App {
  val commandShell: EthereumShell = new EthereumShell

  val shellManager: ShellManager = ShellManager.instance
  shellManager.shellStack.push(commandShell)
}
