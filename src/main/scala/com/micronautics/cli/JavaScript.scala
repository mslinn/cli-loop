package com.micronautics.cli

import javax.script.{Bindings, ScriptContext, ScriptEngine, ScriptEngineManager}

object JavaScript {
  lazy val scriptEngineManager = new ScriptEngineManager

  /** This JavaScript interpreter maintains state throughout the life of the program.
    * Multiple eval() invocations accumulate state. */
  lazy val scriptEngine: ScriptEngine = scriptEngineManager.getEngineByName("JavaScript")
}

class JavaScript {
  import com.micronautics.cli.CliLoop.terminal
  import com.micronautics.cli.JavaScript.scriptEngine

  def safeEval(string: String): AnyRef =
    try {
      scriptEngine.eval(string)
    } catch {
      case e: Exception =>
        CliLoop.printRichError(e.getMessage)
        e
    }

  /** Exchange values with JavaScript */
  def peekPoke(): Unit = try {
    terminal.writer.println()
    terminal.flush()

    val bindings: Bindings = scriptEngine.getBindings(ScriptContext.ENGINE_SCOPE)

    scriptEngine.put("ten", 10)
    assert(scriptEngine.get("ten") == 10.asInstanceOf[AnyRef])
    assert(bindings.get("ten") == 10.asInstanceOf[AnyRef])

    bindings.put("twenty", 20)
    assert(bindings.get("twenty") == 20.asInstanceOf[AnyRef])

    safeEval("var twelve = ten + 2")
    assert(scriptEngine.get("twelve") == 12.asInstanceOf[AnyRef])

    assert(safeEval("twelve") == 12.asInstanceOf[AnyRef])
    assert(safeEval("twelve * 2") == 24.asInstanceOf[AnyRef])
    assert(bindings.get("twelve") == 12.asInstanceOf[AnyRef])
  } catch {
    case e: Exception =>
      CliLoop.richError(e.getMessage)
      ()
  }
}
