package com.micronautics.cli

import java.io.File
import javax.script.{Invocable, ScriptEngineFactory}
import com.micronautics.evaluator._
import org.junit.runner.RunWith
import org.python.core.{PyObject, PySystemState, PyType}
import org.scalatest.Matchers._
import org.scalatest._
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class TestyMcTestFace extends WordSpec with MustMatchers {
  "Persistence" should {
    val file = File.createTempFile("pickle", ".def")
    file.deleteOnExit()
    val js = new JavaScriptEvaluator(useClassloader = false)
    val pickler = new Persistable(file)

    "work" in {
      pickler.write(js.persistableBindings)
      val actual: List[(String, AnyRef)]= js.fromPersistence(pickler.read[List[(String, AnyRef)]])
      actual mustBe js.persistableBindings

      js.bindings.put("int1", 1)
      pickler.write(js.persistableBindings)
      val actual2: List[(String, AnyRef)]  = js.fromPersistence(pickler.read[List[(String, AnyRef)]])
      actual2 mustBe js.persistableBindings

      js.bindings.put("string1", "hello")
      pickler.write(js.persistableBindings)
      val actual3: List[(String, AnyRef)]  = js.fromPersistence(pickler.read[List[(String, AnyRef)]])
      actual3 mustBe js.persistableBindings

      js.bindings.put("double1", 1.2)
      pickler.write(js.persistableBindings)
      val actual4: List[(String, AnyRef)]  = js.fromPersistence(pickler.read[List[(String, AnyRef)]])
      actual4 mustBe js.persistableBindings

      file.delete()
    }
  }

  "ClojureEvaluator" should {
    "work" ignore {
      val clojure = new ClojureEvaluator(useClassloader = false)

      clojure.scriptEngineOk shouldBe true
      val engineFactories: List[ScriptEngineFactory] = clojure.scriptEngineFactories
      engineFactories.size should be > 0

      clojure.showEngineFactories(engineFactories)
      clojure.scriptEngine should not be null
      clojure.scriptEngine.getFactory.getLanguageName shouldBe "Clojure"

      clojure.isDefined("ten")   shouldBe false
      clojure.put("ten", 10)     shouldBe 10.0
      clojure.get("ten")         shouldBe 10.0
      clojure.isDefined("ten")   shouldBe true

      clojure.put("twenty", 20)  shouldBe 20.0
      clojure.get("twenty")      shouldBe 20.0

      clojure.eval("var twelve = ten + 2")
      clojure.get("twelve")      shouldBe 12.0

      clojure.eval("twelve")     shouldBe Some(12.0)
      clojure.eval("twelve * 2") shouldBe Some(24)
      clojure.get("twelve")      shouldBe 12
    }
  }

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
      groovy.put("ten", 10)     shouldBe 10
      groovy.get("ten")         shouldBe 10
      groovy.isDefined("ten")   shouldBe true

      groovy.put("twenty", 20)  shouldBe 20
      groovy.get("twenty")      shouldBe 20

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
    }
  }

  "JavaEvaluator" should {
    "work" ignore {
      val java = new JavaEvaluator(useClassloader = false)

      java.scriptEngineOk shouldBe true
      val engineFactories: List[ScriptEngineFactory] = java.scriptEngineFactories
      engineFactories.size should be > 0

      java.showEngineFactories(engineFactories)
      java.scriptEngine should not be null
      java.scriptEngine.getFactory.getLanguageName shouldBe "Java"

      java.bindingsGlobal.containsKey("ten")
      java.bindingsEngine.containsKey("ten")
      java.isDefined("ten")   shouldBe false
      java.put("ten", 10)     shouldBe 10.0
      java.get("ten")         shouldBe 10.0
      java.isDefined("ten")   shouldBe true

      java.put("twenty", 20)  shouldBe 20.0
      java.get("twenty")      shouldBe 20.0

      java.eval("var twelve = ten + 2")
      java.get("twelve")      shouldBe 12.0

      java.eval("twelve")     shouldBe Some(12.0)
      java.eval("twelve * 2") shouldBe Some(24)
      java.get("twelve")      shouldBe 12
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

  "JRubyEvaluator" should {
    "work" in {
      val jruby = new JRubyEvaluator(useClassloader = false)

      jruby.scriptEngineOk shouldBe true
      val engineFactories: List[ScriptEngineFactory] = jruby.scriptEngineFactories
      engineFactories.size should be > 0

      jruby.showEngineFactories(engineFactories)
      jruby.scriptEngine should not be null
      jruby.scriptEngine.getFactory.getLanguageName shouldBe "ruby"

      jruby.isDefined("ten")   shouldBe false
      jruby.put("ten", 10)     shouldBe 10
      jruby.get("ten")         shouldBe 10
      jruby.isDefined("ten")   shouldBe true

      jruby.put("twenty", 20)  shouldBe 20
      jruby.get("twenty")      shouldBe 20

      // Seems to define a new variable, but that variable is not added to bindings.
      // The new value is returned fine, however
      val Some(twelve) = jruby.eval("twelve = 12")
      twelve                   shouldBe 12
//      jruby.get("twelve")      shouldBe 12 // Not bound, so fails

      //jruby.eval("twelve = ten + 2")  // NameError: undefined local variable or method `ten' for main:Object
//      Did you mean?  test
//        <main> at <script>:1

//      jruby.get("twelve")      shouldBe 12  // Fails, unlike Nashorn and Jython JSR223 implementations

//      jruby.eval("twelve")     shouldBe Some(12) // twelve is not bound
//      jruby.eval("twelve * 2") shouldBe Some(24)
//      jruby.get("twelve")      shouldBe 12
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

      jython.isDefined("ten")   shouldBe false
      jython.put("ten", 10)     shouldBe 10
      jython.get("ten")         shouldBe 10
      jython.isDefined("ten")   shouldBe true

      jython.put("twenty", 20)  shouldBe 20
      jython.get("twenty")      shouldBe 20

      jython.eval("twelve = ten + 2")
      jython.get("twelve")      shouldBe 12

      jython.eval("twelve")     shouldBe Some(12)
      jython.eval("twelve * 2") shouldBe Some(24)
      jython.get("twelve")      shouldBe 12
    }
  }

  "ScalaEvaluator" should {
    "work" in {
      val scala = new ScalaEvaluator(useClassloader = false)

      scala.scriptEngineOk shouldBe true
      val engineFactories: List[ScriptEngineFactory] = scala.scriptEngineFactories
      engineFactories.size should be > 0

      scala.showEngineFactories(engineFactories)
      scala.scriptEngine should not be null
      scala.scriptEngine.getFactory.getLanguageName shouldBe "Scala"

      scala.isDefined("ten")   shouldBe false
      scala.put("ten", 10)     shouldBe 10
      scala.get("ten")         shouldBe 10
      scala.isDefined("ten")   shouldBe true

      scala.put("twenty", 20)  shouldBe 20
      scala.get("twenty")      shouldBe 20

//      scala.eval("var twelve = ten + 2")   // this implementation is also deficient
//      scala.get("twelve")      shouldBe 12

//      scala.eval("twelve")     shouldBe Some(12)
//      scala.eval("twelve * 2") shouldBe Some(24)
//      scala.get("twelve")      shouldBe 12
    }
  }
}
