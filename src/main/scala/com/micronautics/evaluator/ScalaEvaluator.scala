package com.micronautics.evaluator

/** {{{
  * Engine name      = Scala REPL
  * Engine version   = 2.0
  * Language name    = Scala
  * Language version = version 2.12.4
  * Names that can be used to retrieve this engine = scala
  * }}}
  * @param useClassloader set false for unit tests; see [[https://github.com/sbt/sbt/issues/1214]] */
class ScalaEvaluator(useClassloader: Boolean = true) extends JSR223Evaluator[ScalaEvaluator]("scala", useClassloader)
