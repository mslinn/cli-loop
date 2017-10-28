package com.micronautics.cli

object JavaScript {
}

class JavaScript() {
  import javax.script.{Bindings, ScriptContext, ScriptEngine, ScriptEngineManager}
  import com.micronautics.cli.CliLoop.terminal

  protected lazy val scriptEngineManager = new ScriptEngineManager

  /** This JavaScript interpreter maintains state throughout the life of the program.
    * Multiple eval() invocations accumulate state. */
  protected lazy val scriptEngine: ScriptEngine = scriptEngineManager.getEngineByName("JavaScript")

  def safeEval(string: String): AnyRef =
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
      scriptEngine.put("ten", 10)
      assert(scriptEngine.get("ten") == 10.asInstanceOf[AnyRef])
      assert(bindings.get("ten") == 10.asInstanceOf[AnyRef])

      bindings.put("twenty", 20)
      assert(bindings.get("twenty") == 20.asInstanceOf[AnyRef])

      scriptEngine.eval("var twelve = ten + 2")
      assert(scriptEngine.get("twelve") == 12.asInstanceOf[AnyRef])

      assert(scriptEngine.eval("twelve") == 12.asInstanceOf[AnyRef])
      assert(scriptEngine.eval("twelve * 2") == 24.asInstanceOf[AnyRef])
      assert(bindings.get("twelve") == 12.asInstanceOf[AnyRef])
    } catch {
      case e: Exception =>
        CliLoop.richError(e.getMessage)
    }
    this
  }

  protected def bindings: Bindings = scriptEngine.getBindings(ScriptContext.ENGINE_SCOPE)
}
