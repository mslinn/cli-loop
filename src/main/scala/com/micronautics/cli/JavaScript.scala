package com.micronautics.cli

object JavaScript {
}

class JavaScript {
  import javax.script.{Bindings, ScriptContext, ScriptEngine, ScriptEngineManager}
  import com.micronautics.cli.CliLoop.terminal

  protected[cli] lazy val scriptEngineManager = new ScriptEngineManager

  /** This JavaScript interpreter maintains state throughout the life of the program.
    * Multiple eval() invocations accumulate state. */
  protected[cli] lazy val scriptEngine: ScriptEngine = scriptEngineManager.getEngineByName("JavaScript")

  def eval(string: String): AnyRef =
    try {
      terminal.writer.println()
      terminal.flush()
      scriptEngine.eval(string)
    } catch {
      case e: Exception =>
        CliLoop.printRichError(e.getMessage)
        e
    }

  /** Initialize JavaScript instance */
  def setup(): JavaScript = {
    try {
    } catch {
      case e: Exception =>
        CliLoop.richError(e.getMessage)
    }
    this
  }

  def show(expression: String): JavaScript = {
    val result: AnyRef = eval(expression)
    println(s"$expression => $result")
    this
  }

  protected[cli] def bindings: Bindings = scriptEngine.getBindings(ScriptContext.ENGINE_SCOPE)
}
