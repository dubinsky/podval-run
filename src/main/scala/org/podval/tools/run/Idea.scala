package org.podval.tools.run

import java.io.File

object Idea extends Tool.Ide {
  override def toString: String = "Idea"

  protected override def isPresent(environment: Environment): Boolean =
    environment.isJarPresent("/lib/idea_rt")

  // Surely Idea isn't being used as the only build tool for the project:
  // Gradle/Maven is the build tool, and Idea imports from that.
  // But, if 'Create separate module per source set' setting is checked,
  // the only way to figure out project root is by looking at Idea's classpsth :(
  protected override def explodedSuffix   : String = Tool.impossibleSuffix
  protected override def warSuffix        : String = Tool.impossibleSuffix
  protected override def mainClassesSuffix: String = "/out/production/classes"
  protected override def testClassesSuffix: String = "/out/test/classes"
  protected override def jarsSuffix       : String = Tool.impossibleSuffix

  protected override def getProjectRoot(directory: File): Option[File] = Some(directory)

  protected override def getProjectRoots(environment: Environment): Set[File] = Set.empty

  protected override def isTest(environment: Environment): Boolean =
    environment.isPropertyPresent("idea.test.cyclic.buffer.size") &&
      (environment.javaCommand.contains("com.intellij.rt.execution.junit.JUnitStarter") &&
       environment.isJarPresent("/lib/junit-rt")) ||
      (environment.javaCommand.startsWith("org.testng.RemoteTestNGStarter") &&
       environment.isJarPresent("/lib/testng-plugin"))


  protected override def isAppMain(environment: Environment): Boolean =
    // Seems to be obsolete :)
    environment.javaCommand.startsWith("com.intellij.rt.execution.application.AppMain") &&
    environment.isPropertyPresent("idea.launcher.port") &&
    environment.isPropertyPresent("idea.launcher.bin.path")

  protected override def isDebug(environment: Environment): Boolean =
    // For Maven projects, Idea doesn't provide any indications when running in debug mode :(
    environment.isJarPresent("/plugins/Groovy/lib/agent/gragent")
}
