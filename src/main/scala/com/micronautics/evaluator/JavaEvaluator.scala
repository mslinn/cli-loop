package com.micronautics.evaluator

/** {{{
  * Engine name      = MiniMaven
  * Engine version   = 0.0
  * Language name    = Java
  * Language version = 0.4.1
  * Names that can be used to retrieve this engine = Java
  * }}}
  * @param useClassloader set false for unit tests; see [[https://github.com/sbt/sbt/issues/1214]] */
class JavaEvaluator(useClassloader: Boolean = true) extends JSR223Evaluator[JavaEvaluator]("Java", useClassloader)
