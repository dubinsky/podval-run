package org.podval.tools.run

import java.io.File
import Util.{hasFile, ancestors}

object Gradle extends Tool.Build {
  override def toString: String = "Gradle"

  protected override def isPresent(environment: Environment): Boolean = false

  // Gradle doesn't support exploded WARs officially; this is when run from Idea.
  protected override def explodedSuffix   : Seq[String] = Seq("build", "libs", "exploded")
  protected override def warSuffix        : Seq[String] = Seq("build", "libs", "???") // TODO
  protected override def jarsSuffix       : Seq[String] = Seq("build", "libs")
  protected override def mainClassesSuffix: Seq[Seq[String]] = Seq(
    Seq("build", "classes", "main"),
    Seq("build", "classes", "*", "main") // Gradle 5+ splits classes by language
  )
  protected override def testClassesSuffix: Seq[Seq[String]] = Seq(
    Seq("build", "classes", "test"),
    Seq("build", "classes", "*", "test") // Gradle 5+ splits classes by language
  )

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
