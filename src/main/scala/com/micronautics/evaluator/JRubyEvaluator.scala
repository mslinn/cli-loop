package com.micronautics.evaluator

/** {{{
  * Engine name      = JSR 223 JRuby Engine
  * Engine version   = 9.1.14.0
  * Language name    = ruby
  * Language version = jruby 9.1.14.0
  * Names that can be used to retrieve this engine = ruby, jruby
  * }}}
  * @param useClassloader set false for unit tests; see [[https://github.com/sbt/sbt/issues/1214]] */
class JRubyEvaluator(useClassloader: Boolean = true) extends JSR223Evaluator[JRubyEvaluator]("ruby", useClassloader) {
  // See https://github.com/jruby/jruby/issues/1952#issuecomment-360546825
  System.setProperty("org.jruby.embed.localvariable.behavior", "persistent")
}
