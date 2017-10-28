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
      js.put("ten", 10)
      js.get("ten")         shouldBe 10.0
      js.get("ten")         shouldBe 10.0

      js.put("twenty", 20)
      js.get("twenty")      shouldBe 20.0

      js.eval("var twelve = ten + 2")
      js.get("twelve")      shouldBe 12.0

      js.eval("twelve")     shouldBe 12.0
      js.eval("twelve * 2") shouldBe 24.0
      js.get("twelve")      shouldBe 12.0
    }
  }
}
