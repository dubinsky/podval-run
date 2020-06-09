package org.podval.tools.run

import java.io.File

// TODO handle ScalaTest.
sealed trait Run {
  def getWeb: Option[WebApp.InServer]
  final def isWeb: Boolean = getWeb.nonEmpty
  def ide: Option[Tool.Ide]
  def buildTool: Option[Tool.Build]
  def projectRoot: Option[File]
  final def isDevelopment: Boolean = projectRoot.isDefined
  def isTest: Boolean
  def isDebug: Boolean

  protected def toStringCommon: String =
    (if (ide        .isEmpty) "" else s"ide         = ${ide        .get}\n") +
    (if (buildTool  .isEmpty) "" else s"buildTool   = ${buildTool  .get}\n") +
    (if (projectRoot.isEmpty) "" else s"projectRoot = ${projectRoot.get}\n") +
    s"isDebug     = $isDebug\n"
}

object Run {
  case class Web(
    web: WebApp.InServer,
    override val ide: Option[Tool.Ide],
    override val buildTool: Option[Tool.Build],
    override val projectRoot: Option[File],
    override val isDebug: Boolean
  ) extends Run {
    def getWeb: Option[WebApp.InServer] = Some(web)
    override def isTest: Boolean = false
    override def toString: String = s"WebApp $web\n" + toStringCommon
  }

  case class Test(
    override val ide: Option[Tool.Ide],
    override val buildTool: Option[Tool.Build],
    override val projectRoot: Option[File],
    override val isDebug: Boolean
  ) extends Run {
    override def getWeb: Option[WebApp.InServer] = None
    override def isTest: Boolean = true
    override def toString: String = s"Test\n" + toStringCommon
  }

  case class AppMain(
    override val ide: Option[Tool.Ide],
    override val buildTool: Option[Tool.Build],
    override val projectRoot: Option[File],
    override val isDebug: Boolean
  ) extends Run {
    override def getWeb: Option[WebApp.InServer] = None
    override def isTest: Boolean = false
    override def toString: String = s"AppMain\n" + toStringCommon
  }

  def get: Either[Descriptor, Run] = getDescriptor.toRun

  def getProjectRoot: File = get.right.get.projectRoot.get

  final case class Descriptor(
    environment: Environment,
    toolRuns: Set[Tool.Run],
    ide: Error.OrOption[Tool.Ide],
    buildTool: Error.OrOption[Tool.Build],
    web: Error.OrOption[WebApp.InServer],
    projectRoot: Error.OrOption[File],
    isTest: Error.Or[Boolean],
    isDebug: Error.Or[Boolean]
  ) {
    override def toString: String = {
      def optional[T](name: String, value: Error.OrOption[T]): String =
        value.fold(_.toString + "\n", value => if (value.isEmpty) "" else s"$name= ${value.get}\n")

      environment.toString + "\n" +
        toolRuns.map(_.toString).mkString("\n") + "\n" +
        s"Run:\n" +
        optional("ide         ", ide) +
        optional("buildTool   ", buildTool) +
        optional("web         ", web) +
        optional("projectRoot ", projectRoot) +
        isTest.fold(_.toString + "\n", value => s"isTest      = $value\n") +
        s"isDebug     = $isDebug\n"
    }

    def toRun: Either[Descriptor, Run] = {
      val isValid: Boolean = ide.isRight && buildTool.isRight && web.isRight && projectRoot.isRight &&
        isTest.isRight && isDebug.isRight

      if (!isValid) Left(this) else Right {
        val ideReal: Option[Tool.Ide] = ide.right.get
        val buildToolReal: Option[Tool.Build] = buildTool.right.get
        val projectRootReal: Option[File] = projectRoot.right.get
        val isDebugReal: Boolean = isDebug.right.get

        if (web.right.get.isDefined) {
          Run.Web(
            web = web.right.get.get,
            ide = ideReal,
            buildTool = buildToolReal,
            projectRoot = projectRootReal,
            isDebug = isDebugReal
          )
        } else
        if (isTest.right.get) {
          Run.Test(
            ide = ideReal,
            buildTool = buildToolReal,
            projectRoot = projectRootReal,
            isDebug = isDebugReal
          )
        } else {
          Run.AppMain(
            ide = ideReal,
            buildTool = buildToolReal,
            projectRoot = projectRootReal,
            isDebug = isDebugReal
          )
        }
      }
    }
  }

  // TODO parameterize Run to avoid the cast
  val ides: Set[Tool.Ide] = Set(Idea)
  val buildTools: Set[Tool.Build] = Set(Gradle, Maven)
  val appServers: Set[WebApp.Server] = Set(Tomcat)

  lazy val getDescriptor: Descriptor = {
    import Error.atMostOne

    val environment: Environment = Environment.get

    val appServer: Error.OrOption[WebApp.Server] = atMostOne("appServer"  , appServers.filter(_.isPresent(environment)))
    val wars: Set[WebApp] = environment.wars.map(WebApp.War)
    val explodedWars: Set[WebApp] = environment.explodedWars.map(directory =>
      Util.recognize(directory, Seq("src", "main", "webapp"))
        .fold[WebApp](WebApp.Exploded(directory))(module => WebApp.Inplace(directory, module)))
    val webApp: Error.OrOption[WebApp] = atMostOne("webApp", wars ++ explodedWars)

    val web: Error.OrOption[WebApp.InServer] = {
      val isAppServerPresent: Boolean = appServer.isRight && appServer.right.get.nonEmpty
      val isWebAppPresent: Boolean = webApp.isRight && webApp.right.get.nonEmpty
      if (!isWebAppPresent && isAppServerPresent) Left(Error(s"${appServer.right.get.get} present with no WebApp?")) else
      if (isWebAppPresent && !isAppServerPresent) Left(Error(s"${webApp.right.get.get} present with no WebApp.Server?")) else
      if (isWebAppPresent && isAppServerPresent)  Right(Some(WebApp.InServer(webApp.right.get.get, appServer.right.get.get))) else
        Right(None)
    }

    val webAppParameter: Option[WebApp] = webApp.fold(_ => None, value => value)

    def describe[T <: Tool](tools: Set[T], projectRootCandidates: Set[File], what: String): (
      Set[Tool.Run],
      Error.OrOption[T],
      Set[File]) =
    {
      val runs: Set[Tool.Run] = tools.flatMap(_.describe(environment, webAppParameter, projectRootCandidates))
      val runOrError: Error.OrOption[Tool.Run] = atMostOne(what, runs)
      val toolOrError: Error.OrOption[T] = runOrError.right.map(_.map(_.tool.asInstanceOf[T]))
      val projectRoots: Set[File] = runOrError.getOrElse(None).map(_.projectRoots).getOrElse(Set.empty)
      (runs, toolOrError, projectRoots)
    }

    val (ideRuns, ide, ideProjectRoots) =
      describe[Tool.Ide](ides, Set.empty, "ide")

    val (buildToolRuns, buildTool, buildToolProjectRoots) =
      describe[Tool.Build](buildTools, ideProjectRoots, "buildTool")

    val projectRoot: Error.OrOption[File] = atMostOne("projectRoot",
      if (buildToolProjectRoots.nonEmpty) buildToolProjectRoots else ideProjectRoots)

    val toolRuns: Set[Tool.Run] = ideRuns ++ buildToolRuns

    val isProTest: Boolean = toolRuns.exists(_.isTest)
    val isAntiTest: Boolean = toolRuns.exists(_.isAppMain)
    val isTest: Error.Or[Boolean] =
      if (isProTest && isAntiTest) Left(Error("Conflicting 'isTest' and 'isAppMain'")) else
      if (isProTest && webAppParameter.isDefined) Left(Error("Conflicting 'isTest' and a webApp")) else
        Right(isProTest)

    val isDebug: Error.Or[Boolean] = {
      val isIdePresent: Boolean = ideRuns.nonEmpty
      val result: Boolean = toolRuns.exists(_.isDebug)
      if (result && !isIdePresent) Left(Error("Debugging without IDE?")) else Right(result)
    }

    Descriptor(
      environment = environment,
      toolRuns    = toolRuns,
      ide         = ide,
      buildTool   = buildTool,
      web         = web,
      projectRoot = projectRoot,
      isTest      = isTest,
      isDebug     = isDebug
    )
  }
}
