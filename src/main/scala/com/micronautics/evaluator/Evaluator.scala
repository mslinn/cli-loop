package com.micronautics.evaluator

trait Evaluator {
  def init(): EvaluatorInfo

  /** User input is directed verbatim towards an [[Evaluator]] subclass by passing it to this synchronous method;
    * the results of the evaluation are returned verbatim
    * @param text normally contains an entire user string, unparsed
    * @return result from evaluator */
  def input(text: String): AnyRef

  def setup(): Evaluator

  def shutdown(): EvaluatorStatus

  /** Evaluator status query */
  def status: EvaluatorStatus
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
