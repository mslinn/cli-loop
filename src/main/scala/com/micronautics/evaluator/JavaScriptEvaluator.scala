package com.micronautics.evaluator

/** {{{
  * Engine name      = Oracle Nashorn
  * Engine version   = 1.8.0_151
  * Language name    = ECMAScript
  * Language version = ECMA - 262 Edition 5.1
  * Names that can be used to retrieve this engine = nashorn, Nashorn, js, JS, JavaScript, javascript, ECMAScript, ecmascript
  * }}}
  * @param useClassloader set false for unit tests; see [[https://github.com/sbt/sbt/issues/1214]] */
class JavaScriptEvaluator(useClassloader: Boolean = true)
  extends JSR223Evaluator[JavaScriptEvaluator]("JavaScript", useClassloader)
