package com.micronautics.evaluator

import org.slf4j.{Logger, LoggerFactory}

object Evaluator {
  val log: Logger = LoggerFactory.getLogger(classOf[Evaluator[_]].getSimpleName)
}

trait Evaluator[T] {
  var _linesInput: Int = 0
  var _lastErrorInputLine: Option[Int] = None
  var _lastErrorMessage: Option[String] = None

  def init(): EvaluatorInfo

  /** User input is directed verbatim towards an [[Evaluator]] subclass by passing it to this synchronous method;
    * the results of the evaluation are returned verbatim
    * @param text normally contains an entire user string, unparsed
    * @return result from evaluator */
  def eval(text: String): Option[AnyRef]

  def setup(): T

  def shutdown(): EvaluatorStatus = EvaluatorStatus(
    linesInput = _linesInput,
    lastErrorInputLine = _lastErrorInputLine,
    lastErrorMessage = _lastErrorMessage
  )

  /** Evaluator status query */
  def status = EvaluatorStatus(
    linesInput = _linesInput,
    lastErrorInputLine = _lastErrorInputLine,
    lastErrorMessage = _lastErrorMessage
  )
  /** Does not do anything, designed to be overridden */
  def syncFromGlobalBindings(): Unit = {}

  /** Does not do anything, designed to be overridden */
  def syncToGlobalBindings(): Unit = {}
}

case class EvaluatorInfo (
  engineName: String,
  engineVersion: String,
  evaluatorName: String,
  evaluatorVersion: String,
  names: List[String],
  extraInfo: String = ""
) {
  override def toString: String =
    s"""JavaScript engine: $engineName v$engineVersion
       |$evaluatorName / $evaluatorVersion
       |""".stripMargin
}

case class EvaluatorStatus (
  linesInput: Int = 0,
  lastErrorInputLine: Option[Int] = None,
  lastErrorMessage: Option[String] = None
) {
  override def toString: String = {
    val lastError: String = (
      for {
        lastLine    <- lastErrorInputLine
        lastMessage <- lastErrorMessage
      } yield s";\nLast error was '$lastMessage' on line $lastLine"
    ).getOrElse("")
    //s"$linesInput lines input$lastError."
    lastError
 }
}
