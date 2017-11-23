package org.podval.tools.run

import java.io.File
import Util.{hasFile, ancestors}

object Gradle extends Tool.Build {
  override def toString: String = "Gradle"

  protected override def isPresent(environment: Environment): Boolean = false

  // Gradle doesn't support exploded WARs officially; this is when run from Idea.
  protected override def explodedSuffix   : String = "/build/libs/exploded"
  protected override def warSuffix        : String = "/build/libs/???" // TODO
  protected override def mainClassesSuffix: String = "/build/classes/main"
  protected override def testClassesSuffix: String = "/build/classes/test"
  protected override def jarsSuffix       : String = "/build/libs"

  protected override def getProjectRoot(directory: File): Option[File] =
    ancestors(directory)
      .find(hasFile(_, "settings.gradle"))
      .orElse(if (hasFile(directory, "build.gradle")) Some(directory) else None)

  protected override def getProjectRoots(environment: Environment): Set[File] = Set.empty

  protected override def isTest(environment: Environment): Boolean =
    environment.isJarPresent("gradle-worker") &&
    environment.javaCommand.startsWith("worker.org.gradle.process.internal.worker.GradleWorkerMain") &&
    environment.isPropertyPresent("org.gradle.test.worker")

  protected override def isAppMain(environment: Environment): Boolean =
    environment.isPropertyPresent("org.gradle.appname")

  protected override def isDebug(environment: Environment): Boolean = false
}
