package com.micronautics.evaluator

/** Unsure what this means, or what to do:
  *
  * "The engine keeps per default hard references to the script functions.
  * To change this you should set a engine level scoped attribute to the script context of the name #jsr223.groovy.engine.keep.globals
  * with a String being phantom to use phantom references, weak to use weak references or soft to use soft references -
  * casing is ignored. Any other string will cause the use of hard references."
  *
  * @see See [[http://groovy-lang.org/integrating.html#jsr223]]
  * @param useClassloader set false for unit tests; see [[https://github.com/sbt/sbt/issues/1214]] */
class GroovyEvaluator(useClassloader: Boolean = true) extends JSR223Evaluator[GroovyEvaluator]("groovy", useClassloader)
