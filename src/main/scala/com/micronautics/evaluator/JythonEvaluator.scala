package com.micronautics.evaluator

/** {{{
  * Engine name      = jython
  * Engine version   = 2.7.1
  * Language name    = python
  * Language version = 2.7
  * Names that can be used to retrieve this engine = python, jython
  * }}}
  *
  * @param useClassloader set false for unit tests; see [[https://github.com/sbt/sbt/issues/1214]] */
class JythonEvaluator(useClassloader: Boolean = true) extends JSR223Evaluator[JythonEvaluator]("python", useClassloader)
