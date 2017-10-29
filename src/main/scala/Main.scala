import com.micronautics.cli.ShellManager
import com.micronautics.ethereum.CommandShell

object Main extends App {
  val commandShell = new CommandShell

  val shellManager = new ShellManager
  shellManager.push(commandShell)
}

