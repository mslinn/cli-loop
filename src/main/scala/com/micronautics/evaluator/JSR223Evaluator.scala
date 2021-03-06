package com.micronautics.evaluator

import javax.script.{Bindings, Invocable, ScriptContext, ScriptEngine, ScriptEngineFactory, ScriptEngineManager, ScriptException, SimpleBindings}
import com.micronautics.cli.MainLoop._
import com.micronautics.terminal.TerminalStyles.{printRichError, richError}
import scala.collection.JavaConverters._
import Evaluator.log
import com.micronautics.cli.GlobalConfig

object JSR223Evaluator {
  lazy val globalBindings: SimpleBindings = new SimpleBindings

  lazy val pickler = new Persistable(GlobalConfig.instance.cliHome)

  protected val scriptEngineManager: ScriptEngineManager =
    Option(new ScriptEngineManager())
      .getOrElse {
        Option(new ScriptEngineManager(getClass.getClassLoader))
          .getOrElse(throw new Exception("ScriptEngineManger could not be loaded."))
      }

  implicit class RichBindings(bindings: Bindings) {
    /** @return List[(String, AnyRef)] of variable name/values */
    def asList: List[(String, AnyRef)] = Option(bindings.entrySet).map { simpleBindings =>
      simpleBindings.asScala.map(entry => (entry.getKey, entry.getValue)).toList
    }.getOrElse(Nil)

    /** Loads bindings from disk */
    def load: List[(String, AnyRef)] = {
      val values = pickler.read[List[(String, AnyRef)]]
      values foreach { case (key, value) =>
        bindings.put(key, value)
      }
      values
    }

    /** Writes bindings to disk */
    def save(): List[(String, AnyRef)] = {
      val value = bindings.asList
      pickler.write(value)
      value
    }
  }
}

/** Syncs variables and methods defined in `globalBindings`
  * @param useClassloader set false for unit tests; see [[https://github.com/sbt/sbt/issues/1214]] todo delete this parameter? */
abstract class JSR223Evaluator[T](engineName: String, useClassloader: Boolean = true) extends Evaluator[T] {
  import JSR223Evaluator._

  lazy val scriptEngine: ScriptEngine = {
    log.debug(s"Summoning $engineName")
    scriptEngineManager.getEngineFactories.asScala.find(_.getNames.asScala.contains(engineName))
      .getOrElse {
        val factoryNames: String = scriptEngineManager.getEngineFactories.asScala.flatMap { factory =>
          factory.getNames.asScala
        }.sorted.mkString(", ")
        log.error(s"Error: ScriptEngine $engineName not found. Available scriptEngines are: $factoryNames.")
        sys.exit(1)
      }
  }.getScriptEngine

  lazy val engineContext: ScriptContext = scriptEngine.getContext

  lazy val factory: ScriptEngineFactory = scriptEngine.getFactory

  lazy val invocable: Invocable = scriptEngine.asInstanceOf[Invocable]


  def bindings: javax.script.Bindings = engineContext.getBindings(ScriptContext.ENGINE_SCOPE)

  /** Deletes all user variables from memory; affects all evaluators because `globalBindings` is also cleared */
  def clearBindings(): Unit = {
    globalBindings.clear()
    engineContext.setBindings(globalBindings, ScriptContext.ENGINE_SCOPE)
  }

  /** Loads variables and methods into this evaluator from globalBindings */
  override def syncFromGlobalBindings(): Unit =
    globalBindings.asList foreach { case (key, value) =>
      bindings.put(key, value)
    }

  /** Saves variables and methods from this evaluator to globalBindings */
  override def syncToGlobalBindings(): Unit =
    bindings.asList foreach { case (key, value) =>
      globalBindings.put(key, value)
    }

  override def init(): EvaluatorInfo = {
    _linesInput = 0
    _lastErrorInputLine = None
    _lastErrorMessage = None

    bindings.load foreach { case (key, value) =>
      bindings.put(key, value)
    }
    syncToGlobalBindings()

    info
  }

  def eval(string: String): Option[AnyRef] =
    try {
      _linesInput = _linesInput + 1
      if (null==scriptEngine) {
        scriptEngineOk
        None
      } else {
        val value = scriptEngine.eval(string, bindingsEngine)
        val result: Any = value match {
          case x: java.lang.Boolean => Some(Boolean.unbox(x))
          case x: java.lang.Double  => Some(Double.unbox(x))
          case x: java.lang.Float   => Some(Float.unbox(x))
          case x: java.lang.Integer => Some(Int.unbox(x))
          case x: AnyRef => Some(x)
          case x if x == null => None
          case x => Some(x.toString) // this is the only way to return something like this
        }
        result.asInstanceOf[Option[AnyRef]]
      }
    } catch {
      case e: NullPointerException =>
        printRichError(s"Error: ${ e.getMessage }")
        None

      case e: ScriptException =>
        printRichError(s"Error on line ${ e.getLineNumber }, column ${ e.getColumnNumber}: ${ e.getMessage }")
        None

      case e: Exception =>
        val cause: String = {
          val x = if (null!=e.getCause) e.getCause.getMessage else ""
          if (x.nonEmpty) s"cause: $x, " else ""
        }
        val message: String = {
          val x = e.getMessage
          if (x.nonEmpty) s"$x, " else ""
        }
        val stackTrace = e.getStackTrace.mkString("\n")
        printRichError(s"$engineName evaluation error: $cause $message $stackTrace")
        None
    }

  def get(name: String): AnyRef = bindingsEngine.get(name)

  /** This JavaScriptEvaluator interpreter maintains state throughout the life of the program.
      * Multiple `eval()` invocations accumulate state. */
  def getScriptEngine(engineName: String): ScriptEngine =
    Option(scriptEngineManager.getEngineByName(engineName)).getOrElse {
      throw new Exception(s"Error: $engineName engine not available.")
    }

  /** {{{
    * Engine name      = jython
    * Engine version   = 2.7.1
    * Language name    = python
    * Language version = 2.7
    * Names that can be used to retrieve this engine = python, jython }}} */
  def info = EvaluatorInfo(
    engineName       = factory.getEngineName,
    engineVersion    = factory.getEngineVersion,
    evaluatorName    = factory.getLanguageName,
    evaluatorVersion = s"${ factory.getLanguageVersion }",
    names            = factory.getNames.asScala.toList
  )

  def isDefined(name: String): Boolean = bindingsEngine.containsKey(name)

  /** All numbers in JavaScriptEvaluator are doubles: that is, they are stored as 64-bit IEEE-754 doubles.
    * JavaScriptEvaluator does not have integers, so before an `Int` can be provided to the `value` parameter
    * it is first implicitly converted to `Double`. */
  def put(name: String, value: AnyVal): AnyRef = {
    bindingsEngine.put(name, value)
    val retrieved: AnyRef = bindingsEngine.get(name)
    retrieved
  }

  def scopeKeysEngine: Set[String] = bindingsEngine.keySet.asScala.toSet

  def scriptEngineFactories: List[ScriptEngineFactory] = {
    scriptEngineOk
    val factories = scriptEngineManager.getEngineFactories
    println(s"${ factories.size } scripting engines are available.")
    factories.asScala.toList
  }

  def scriptEngineOk: Boolean = {
    if (scriptEngineManager==null) {
      Console.err.println("\nError: scriptEngineManager is null!")
      System.exit(0)
    }
    scriptEngineManager.getEngineFactories.asScala.exists(_.getNames.contains(engineName))
  }

  /** Loads variables and methods from globalBindings */
  override def setup(): T = {
    try {
      syncFromGlobalBindings()
      // todo reload context from path previous session
    } catch {
      case e: Exception =>
        richError(e.getMessage)
    }
    this.asInstanceOf[T]
  }

  /* Sample output for Windows (Linux does not show the Scala REPL):
      2 scripting engines are available.
      Engine Name      = Scala REPL
      Engine Version   = 2.0
      Language Name    = Scala
      Language Version = version 2.12.4
      Names = scala

      Engine Name      = Oracle Nashorn
      Engine Version   = 1.8.0_151
      Language Name    = ECMAScript
      Language Version = ECMA - 262 Edition 5.1
      Names = nashorn, Nashorn, js, JS, JavaScriptEvaluator, javascript, ECMAScript, ecmascript */
  def showEngineFactories(engineFactories: List[ScriptEngineFactory]): Unit =
    engineFactories.foreach { engine =>
      println(
        s"""Engine name      = ${ engine.getEngineName }
           |Engine version   = ${ engine.getEngineVersion }
           |Language name    = ${ engine.getLanguageName }
           |Language version = ${ engine.getLanguageVersion }
           |Names that can be used to retrieve this engine = ${ engine.getNames.asScala.mkString(", ") }
           |""".stripMargin)
    }

  /** Saves variables and methods to globalBindings */
  override def shutdown(): EvaluatorStatus = {
    syncFromGlobalBindings()
    // todo save session context somehow
    super.shutdown()
  }

  def bindingsEngine: Bindings = engineContext.getBindings(ScriptContext.ENGINE_SCOPE)

  def bindingsGlobal: Bindings = engineContext.getBindings(ScriptContext.GLOBAL_SCOPE)
}

