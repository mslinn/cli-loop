package com.micronautics.evaluator

/** {{{
  * Engine name      = clojure
  * Engine version   = 0.0
  * Language name    = Clojure
  * Language version = 0.1.6
  * Names that can be used to retrieve this engine = clojure
  * }}}
  * @param useClassloader set false for unit tests; see [[https://github.com/sbt/sbt/issues/1214]] */
class ClojureEvaluator(useClassloader: Boolean = true)
  extends JSR223Evaluator[ClojureEvaluator]("clojure", useClassloader)
