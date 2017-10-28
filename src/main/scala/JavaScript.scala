import javax.script.{ScriptEngine, ScriptEngineManager}

object JavaScript {
  lazy val scriptEngineManager = new ScriptEngineManager
  lazy val nashorn: ScriptEngine = scriptEngineManager.getEngineByName("nashorn")
}

class JavaScript {
  import JavaScript.nashorn.eval
  import CliLoop.terminal

  def safeEval(string: String): AnyRef =
    try {
      eval(string)
    } catch {
      case e: Exception =>
        terminal.writer.print(CliLoop.error(e.getMessage))
        e
    }

  def print: AnyRef = {
    val message = "The null that is displayed next is returned by JavaScript's print function"
    safeEval(s"""print("$message")""")
  }

  def add: Int = try {
    safeEval("var x = 10 + 2;")
    safeEval("x = x + 2;").asInstanceOf[Int]
    safeEval("x").asInstanceOf[Int]
  } catch {
    case e: Exception =>
      CliLoop.error(e.getMessage)
      0
  }

  def demo(): Unit = {
    terminal.writer.println()
    terminal.flush()
    terminal.writer.println(print)
    terminal.writer.println(add)
  }
}
