package com.micronautics.cli

import javax.script.ScriptException

object JavaScript {
}

class JavaScript {
  import javax.script.{Bindings, ScriptContext, ScriptEngine, ScriptEngineManager}
  import collection.JavaConverters._
  import CliLoop.terminal.{writer => TWriter}

  protected lazy val scriptEngineManager = new ScriptEngineManager(getClass.getClassLoader) // https://github.com/sbt/sbt/issues/1214

  /* Sample output:
      2 scripting engines are available.
      Engine Name = Scala REPL
      Engine Version = 2.0
      Language Name = Scala
      Language Version = version 2.12.4
      Names = scala

      Engine Name = Oracle Nashorn
      Engine Version = 1.8.0_151
      Language Name = ECMAScript
      Language Version = ECMA - 262 Edition 5.1
      Names = nashorn, Nashorn, js, JS, JavaScript, javascript, ECMAScript, ecmascript */
  protected def checkScriptEngine(): Unit = {
    if (scriptEngineManager==null) {
      Console.err.println("\nError: scriptEngineManager is null!")
      System.exit(0)
    }

    TWriter.println(scriptEngineManager.getEngineFactories.size + " scripting engines are available.")
    scriptEngineManager.getEngineFactories.asScala.foreach { engine =>
      TWriter.println(s"""Engine Name = ${ engine.getEngineName }
                                         |Engine Version = ${ engine.getEngineVersion }
                                         |Language Name = ${ engine.getLanguageName }
                                         |Language Version = ${ engine.getLanguageVersion }
                                         |Names = ${ engine.getNames.asScala.mkString(", ") }
                                         |""".stripMargin)
    }
  }

  /** This JavaScript interpreter maintains state throughout the life of the program.
    * Multiple eval() invocations accumulate state. */
  def scriptEngine: ScriptEngine = scriptEngineManager.getEngineByName("JavaScript")

  def eval(string: String): AnyRef =
    try {
      if (scriptEngine==null) checkScriptEngine()
      val value = scriptEngine.eval(string)
      val result: Any = value match {
        case x: java.lang.Boolean => Boolean.unbox(x)
        case x: java.lang.Double  => Double.unbox(x)
        case x: java.lang.Float   => Float.unbox(x)
        case x: java.lang.Integer => Int.unbox(x)
        case x =>
          println(s"x=$x")
          x
      }
      CliLoop.printRichInfo(s"$result\n")
      result.asInstanceOf[AnyRef]
    } catch {
      case e: ScriptException =>
        CliLoop.printRichError(s"Error on line ${ e.getLineNumber }, column ${ e.getColumnNumber}: ${ e.getMessage }")
        e

      case e: Exception =>
        CliLoop.printRichError(s"JavaScript.eval - ${ e.getCause }, ${ e.getMessage } ${ e.getStackTrace.mkString("\n") }")
        e
    }

  def get(name: String): AnyRef = scriptEngine.get(name)

  def isDefined(name: String): Boolean = bindings.containsKey(name)

  /** All numbers in JavaScript are doubles: that is, they are stored as 64-bit IEEE-754 doubles.
    * JavaScript does not have integers, so before an `Int` can be provided to the `value` parameter it is first implicitly converted to `Double`. */
  def put(name: String, value: AnyVal): AnyRef = {
    scriptEngine.put(name, value)
    get(name)
  }

  /** Initialize JavaScript instance */
  def setup(): JavaScript = {
    try {
      // one day we might need to reload context from a previous session
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
