package com.micronautics.ethereum

import javax.script.{ScriptEngineFactory, ScriptException}
import com.micronautics.cli.TerminalStyles

/** @param useClassloader set false for unit tests */
class JavaScript(useClassloader: Boolean = true) extends TerminalStyles {
  import javax.script.{Bindings, ScriptContext, ScriptEngine, ScriptEngineManager}
  import scala.collection.JavaConverters._

  protected lazy val scriptEngineManager: ScriptEngineManager =
    if (useClassloader) new ScriptEngineManager(getClass.getClassLoader) // https://github.com/sbt/sbt/issues/1214
    else new ScriptEngineManager()

  def eval(string: String): AnyRef =
    try {
      if (scriptEngine==null) scriptEngineOk
      val globalValue = scriptEngine.eval(string, bindingsGlobal)
      val engineValue = scriptEngine.eval(string, bindingsEngine)
      val result = globalValue match {
        case x: java.lang.Boolean => Boolean.unbox(x)
        case x: java.lang.Double  => Double.unbox(x)
        case x: java.lang.Float   => Float.unbox(x)
        case x: java.lang.Integer => Int.unbox(x)
        case x =>
          println(s"x=$x")
          x
      }
      printRichInfo(s"$result\n")
      result.asInstanceOf[AnyRef]
    } catch {
      case e: ScriptException =>
        printRichError(s"Error on line ${ e.getLineNumber }, column ${ e.getColumnNumber}: ${ e.getMessage }")
        e

      case e: Exception =>
        printRichError(s"JavaScript.eval - ${ e.getCause }, ${ e.getMessage } ${ e.getStackTrace.mkString("\n") }")
        e
    }

  def get(name: String): AnyRef = bindingsGlobal.get(name)

  def isDefined(name: String): Boolean = bindingsGlobal.containsKey(name)

  /** All numbers in JavaScript are doubles: that is, they are stored as 64-bit IEEE-754 doubles.
    * JavaScript does not have integers, so before an `Int` can be provided to the `value` parameter it is first implicitly converted to `Double`. */
  def put(name: String, value: AnyVal): AnyRef = {
    bindingsGlobal.put(name, value)
    val retrieved: AnyRef = bindingsGlobal.get(name)
    retrieved
  }

  /** This JavaScript interpreter maintains state throughout the life of the program.
    * Multiple eval() invocations accumulate state. */
  def scriptEngine: ScriptEngine = {
    import javax.script._

    val engine = scriptEngineManager.getEngineByName("JavaScript")
    val bindings = engine.createBindings
    engine.setBindings(bindings, ScriptContext.ENGINE_SCOPE)
    engine
  }

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
  def scriptEngineFactories: List[ScriptEngineFactory] = {
    scriptEngineOk
    println(scriptEngineManager.getEngineFactories.size + " scripting engines are available.")
    scriptEngineManager.getEngineFactories.asScala.toList
  }

  def scriptEngineOk: Boolean = {
    if (scriptEngineManager==null) {
      Console.err.println("\nError: scriptEngineManager is null!")
      System.exit(0)
    }
    scriptEngineManager.getEngineFactories.size>0
  }

  def scopeKeysEngine: Set[String] = bindingsEngine.keySet.asScala.toSet

  def scopeKeysGlobal: Set[String] = bindingsGlobal.keySet.asScala.toSet

  /** Initialize JavaScript instance */
  def setup(): JavaScript = {
    try {
      // one day we might need to reload context from a previous session
    } catch {
      case e: Exception =>
        richError(e.getMessage)
    }
    this
  }

  def showEngineFactories(engineFactories: List[ScriptEngineFactory]): Unit =
    engineFactories.foreach { engine =>
      println(
        s"""Engine name = ${ engine.getEngineName }
           |Engine version = ${ engine.getEngineVersion }
           |Language name = ${ engine.getLanguageName }
           |Language version = ${ engine.getLanguageVersion }
           |Names that can be used to retrieve this engine = ${ engine.getNames.asScala.mkString(", ") }
           |""".stripMargin)
    }

  def showEvaluation(expression: String): JavaScript = {
    val result: AnyRef = eval(expression)
    println(s"$expression => $result")
    this
  }

  protected[ethereum] def bindingsEngine: Bindings = scriptEngine.getBindings(ScriptContext.ENGINE_SCOPE)

  protected[ethereum] def bindingsGlobal: Bindings = scriptEngine.getBindings(ScriptContext.GLOBAL_SCOPE)
}
