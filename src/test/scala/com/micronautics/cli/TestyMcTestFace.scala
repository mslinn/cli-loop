package com.micronautics.cli

import org.junit.runner.RunWith
import org.scalatest.Matchers._
import org.scalatest._
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class TestyMcTestFace extends WordSpec with MustMatchers {
  val js = new JavaScript

  "JavaScript" should {
    "work" in {
      js.scriptEngine.put("ten", 10)
      js.scriptEngine.get("ten")         shouldBe 10.asInstanceOf[AnyRef]
      js.bindings.get("ten")             shouldBe 10.asInstanceOf[AnyRef]

      js.bindings.put("twenty", 20)
      js.bindings.get("twenty")          shouldBe 20.asInstanceOf[AnyRef]

      js.scriptEngine.eval("var twelve = ten + 2")
      js.scriptEngine.get("twelve")      shouldBe 12.asInstanceOf[AnyRef]

      js.scriptEngine.eval("twelve")     shouldBe 12.asInstanceOf[AnyRef]
      js.scriptEngine.eval("twelve * 2") shouldBe 24.asInstanceOf[AnyRef]
      js.bindings.get("twelve")          shouldBe 12.asInstanceOf[AnyRef]
    }
  }
}
