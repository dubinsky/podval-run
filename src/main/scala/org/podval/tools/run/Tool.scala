package org.podval.tools.run

import java.io.File

sealed trait Tool {
  final def describe(
    environment: Environment,
    webApp: Option[WebApp],
    projectRootCandidates: Set[File]
  ): Option[Tool.Run] = {
    import Util.dropSuffix

    val mainClassesModules   : Set[File] = environment.directories   .flatMap(dropSuffix(_, mainClassesSuffix))
    val testClassesModules   : Set[File] = environment.directories   .flatMap(dropSuffix(_, testClassesSuffix))
    val jarDirectoriesModules: Set[File] = environment.jarDirectories.flatMap(dropSuffix(_, jarsSuffix))

    val webAppModule: Option[File] = webApp.flatMap { _ match {
      case WebApp.War     (root        ) => dropSuffix(root.getParentFile, warSuffix)
      case WebApp.Exploded(root        ) => dropSuffix(root.getParentFile, explodedSuffix)
      case WebApp.Inplace (_   , module) => Some(module)
      }
    }

    val projectRoots: Set[File] = (
      projectRootCandidates ++ getProjectRoots(environment) ++
      mainClassesModules ++ testClassesModules ++ jarDirectoriesModules ++ webAppModule
    ).flatMap(getProjectRoot)

    val isTestValue: Boolean = isTest(environment) || testClassesModules.nonEmpty
    val isAppMainValue: Boolean = isAppMain(environment)
    val isDebugValue: Boolean = isDebug(environment)

    if (!isPresent(environment) &&
      projectRoots.isEmpty && !isTestValue && !isAppMainValue && !isDebugValue) None else Some(Tool.Run(
        tool = this,
        projectRoots = projectRoots,
        isTest = isTestValue,
        isAppMain = isAppMainValue,
        isDebug = isDebugValue
      )
    )
  }

  protected def explodedSuffix   : String
  protected def warSuffix        : String
  protected def mainClassesSuffix: String
  protected def testClassesSuffix: String
  protected def jarsSuffix       : String

  protected def getProjectRoot(directory: File): Option[File]

  protected def getProjectRoots(environment: Environment): Set[File]

  protected def isTest(environment: Environment): Boolean

  protected def isAppMain(environment: Environment): Boolean

  protected def isDebug(environment: Environment): Boolean

  protected def isPresent(environment: Environment): Boolean
}

object Tool {
  trait Ide   extends Tool
  trait Build extends Tool

  final case class Run(
    tool: Tool,
    projectRoots: Set[File],
    isTest: Boolean,
    isAppMain: Boolean,
    isDebug: Boolean
  ) {
    override def toString: String = {
      s"$tool:\n" +
        s"projectRoots = " + projectRoots.mkString(" ") + "\n" +
        s"isTest       = $isTest\n" +
        s"isAppMain    = $isAppMain\n" +
        s"isDebug      = $isDebug\n"
    }
  }

  val impossibleSuffix: String = "/if/this/is/a/part/of/the/classppath/we/have/a/problem/:)"
}
