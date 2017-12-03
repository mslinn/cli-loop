package com.micronautics.evaluator

import org.web3j.codegen.SolidityFunctionWrapperGenerator
import org.web3j.console.{KeyImporter, WalletCreator, WalletSendFunds, WalletUpdater}
import org.web3j.utils.Version

class EthereumEvaluator extends JSR223Evaluator[EthereumEvaluator]("web3j") {
  override def init(): EvaluatorInfo = {
    super.init()
    // todo perform any initialization
    info
  }

  def parseParameterArgument(args: List[String], shortOption: String, longOption: String): String =
    args.sliding(2, 2).collectFirst {
      case option :: value if option==shortOption || option==longOption => value
    }.flatten.mkString

  def parseFlag(args: List[String]): (Boolean, List[String]) = {
    val index = args.indexOf("--solidityTypes")
    val found = index>=0
    (found, if (index >= 0) args.drop(index) else args)
  }

  override def eval(text: String): Option[AnyRef] = text.split(" ").toList match {
    case "wallet" :: args =>
      evalWallet(args)

    case "solidity" :: "generate" :: binaryFileLocation :: absFileLocation :: args =>
      val (useJavaNativeTypes, args2) = parseFlag(args)
      val destinationDirLocation = parseParameterArgument(args2, "-o", "--outputDir")
      val basePackageName = parseParameterArgument(args2, "-p", "--package")
      new SolidityFunctionWrapperGenerator(
                      binaryFileLocation,
                      absFileLocation,
                      destinationDirLocation,
                      basePackageName,
                      useJavaNativeTypes)
                      .generate()

    case "version" :: Nil =>
      println(s"""Version: ${ Version.getVersion }
                 |Built: ${ Version.getTimestamp }
                 |""".stripMargin)
      None

    case _ =>
      None
  }

  private def evalWallet(args: List[String]): Option[AnyRef] = {
    args match {
      case "create" :: args2 =>
        WalletCreator.main(args2.toArray)
        None

      case "update" :: args2 =>
        WalletUpdater.main(args2.toArray)
        None

      case "send" :: args2 =>
        WalletSendFunds.main(args2.toArray)
        None

      case "fromkey" :: args2 =>
        KeyImporter.main(args2.toArray)
        None
    }
  }

  override def setup(): EthereumEvaluator = {
    super.setup()
    // todo perform any configuration
    this
  }

  override def shutdown(): EvaluatorStatus = {
    // todo save session context somehow
    super.shutdown()
  }

  override def info = EvaluatorInfo(
    engineName = "web3j-scala",
    engineVersion = "0.2.0",
    evaluatorName = "web3j-scala",
    evaluatorVersion = s"0.2.0",
    names = List("version", "wallet", "solidity")
  )
}
