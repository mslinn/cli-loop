package com.micronautics.cli

import javax.script.{Invocable, ScriptEngineFactory}
import com.micronautics.evaluator.{GroovyEvaluator, JavaScriptEvaluator, JythonEvaluator}
import org.junit.runner.RunWith
import org.python.core.{PyObject, PySystemState, PyType}
import org.scalatest.Matchers._
import org.scalatest._
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class TestyMcTestFace extends WordSpec with MustMatchers {
  "GroovyEvaluator" should {
    "work" in {
      val groovy = new GroovyEvaluator(useClassloader = false)

      groovy.scriptEngineOk shouldBe true
      val engineFactories: List[ScriptEngineFactory] = groovy.scriptEngineFactories
      engineFactories.size should be > 0

      groovy.showEngineFactories(engineFactories)
      groovy.scriptEngine should not be null
      groovy.scriptEngine.getFactory.getLanguageName shouldBe "Groovy"

      groovy.isDefined("ten")   shouldBe false
      groovy.put("ten", 10)     shouldBe 10.0
      groovy.get("ten")         shouldBe 10.0
      groovy.isDefined("ten")   shouldBe true

      groovy.put("twenty", 20)  shouldBe 20.0
      groovy.get("twenty")      shouldBe 20.0

      val Some(sum) = groovy.eval("(1..10).sum()")
      sum shouldBe 55

      groovy.eval("def factorial(n) { n <= 1 ? 1 : n * factorial(n - 1) }") // returns None
      val invocable: Invocable = groovy.invocable
      invocable.invokeFunction("factorial", 5.asInstanceOf[Object]) shouldBe 120

      // Groovy's JSR223 does not add 'twelve' to the ScriptContext.ENGINE_SCOPE bindings
      // I filed issue [[https://issues.apache.org/jira/browse/GROOVY-8400 GROOVY-8400]]
      val Some(twelve) = groovy.eval("def twelve = ten + 2")
//      groovy.eval("twelve")     shouldBe Some(12)
//      groovy.eval("twelve * 2") shouldBe Some(24)
      twelve shouldBe 12
//      groovy.get("twelve")      shouldBe 12

      groovy.put("y", 99)       shouldBe 99
      groovy.get("y")           shouldBe 99
    }
  }

  "JythonEvaluator" should {
    "work" in {
      val jython = new JythonEvaluator(useClassloader = false)

      jython.scriptEngineOk shouldBe true
      val engineFactories: List[ScriptEngineFactory] = jython.scriptEngineFactories
      engineFactories.size should be > 0

      jython.showEngineFactories(engineFactories)
      jython.scriptEngine should not be null
      jython.scriptEngine.getFactory.getLanguageName shouldBe "python"

      // not sure where this is going, but it looks potentially interesting
      jython.eval("import sys")
      jython.eval("sys").foreach { case sys: PySystemState =>
        val modules: PyObject = sys.modules
        val moduleType: PyType = modules.getType
        val _: PyObject = moduleType.getDict
      }

      val xValue: Object = jython.get("x")
      println("x: " + xValue)

      jython.isDefined("ten")   shouldBe false
      jython.put("ten", 10)     shouldBe 10.0
      jython.get("ten")         shouldBe 10.0
      jython.isDefined("ten")   shouldBe true

      jython.put("twenty", 20)  shouldBe 20.0
      jython.get("twenty")      shouldBe 20.0

      jython.eval("twelve = ten + 2")
      jython.get("twelve")      shouldBe 12.0

      jython.eval("twelve")     shouldBe Some(12.0)
      jython.eval("twelve * 2") shouldBe Some(24)
      jython.get("twelve")      shouldBe 12

      jython.put("y", 99)       shouldBe 99
      jython.get("y")           shouldBe 99
    }
  }

  "JavaScriptEvaluator" should {
    "work" in {
      val js = new JavaScriptEvaluator(useClassloader = false)

      js.scriptEngineOk shouldBe true
      val engineFactories: List[ScriptEngineFactory] = js.scriptEngineFactories
      engineFactories.size should be > 0

      js.showEngineFactories(engineFactories)
      js.scriptEngine should not be null
      js.scriptEngine.getFactory.getLanguageName shouldBe "ECMAScript"

      js.isDefined("ten")   shouldBe false
      js.put("ten", 10)     shouldBe 10.0
      js.get("ten")         shouldBe 10.0
      js.isDefined("ten")   shouldBe true

      js.put("twenty", 20)  shouldBe 20.0
      js.get("twenty")      shouldBe 20.0

      js.eval("var twelve = ten + 2")
      js.get("twelve")      shouldBe 12.0

      js.eval("twelve")     shouldBe Some(12.0)
      js.eval("twelve * 2") shouldBe Some(24)
      js.get("twelve")      shouldBe 12

      js.put("y", 99)       shouldBe 99
      js.get("y")           shouldBe 99
    }
  }
}
