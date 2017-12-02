package com.micronautics.evaluator

/** {{{
  *
  * }}}
  *
  * @param useClassloader set false for unit tests; see [[https://github.com/sbt/sbt/issues/1214]] */
class KotlinEvaluator(useClassloader: Boolean = true) extends JSR223Evaluator[KotlinEvaluator]("kotlin", useClassloader)
