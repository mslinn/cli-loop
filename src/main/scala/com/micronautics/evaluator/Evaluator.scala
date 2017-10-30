package com.micronautics.evaluator

trait Evaluator {
  var linesInput: Int = 0
  var lastErrorInputLine: Option[Int] = None
  var lastErrorMessage: Option[String] = None

  def init(): EvaluatorInfo

  /** User input is directed verbatim towards an [[Evaluator]] subclass by passing it to this synchronous method;
    * the results of the evaluation are returned verbatim
    * @param text normally contains an entire user string, unparsed
    * @return result from evaluator */
  def eval(text: String): AnyRef

  def setup(): Evaluator

  def shutdown(): EvaluatorStatus = EvaluatorStatus(
    linesInput = linesInput,
    lastErrorInputLine = lastErrorInputLine,
    lastErrorMessage = lastErrorMessage
  )

  /** Evaluator status query */
  def status = EvaluatorStatus(
    linesInput = linesInput,
    lastErrorInputLine = lastErrorInputLine,
    lastErrorMessage = lastErrorMessage
  )
}

case class EvaluatorInfo (
  engineName: String,
  engineVersion: String,
  evaluatorName: String,
  evaluatorVersion: String,
  names: List[String],
  extraInfo: String = ""
)

case class EvaluatorStatus (
  linesInput: Int = 0,
  lastErrorInputLine: Option[Int] = None,
  lastErrorMessage: Option[String] = None
)
