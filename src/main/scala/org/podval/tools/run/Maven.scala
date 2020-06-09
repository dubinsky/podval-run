package org.podval.tools.run

import java.io.File
import Util.{hasFile, ancestors}

object Maven extends Tool.Build {
  override def toString: String = "Maven"

  protected override def isPresent(environment: Environment): Boolean = false

  protected override def explodedSuffix   : Seq[String] = Seq("target")
  protected override def warSuffix        : Seq[String] = Seq("target")
  protected override def mainClassesSuffix: Seq[Seq[String]] = Seq(Seq("target", "classes"))
  protected override def testClassesSuffix: Seq[Seq[String]] = Seq(Seq("target", "test-classes"))
  protected override def jarsSuffix       : Seq[String] = Seq(Tool.impossibleSuffix) // TODO

  protected override def getProjectRoot(directory: File): Option[File] =
    ancestors(directory).takeWhile(hasFile(_, "pom.xml")).lastOption

  protected override def getProjectRoots(environment: Environment): Set[File] =
    (environment.properties.get("basedir") ++ environment.properties.get("maven.multiModuleProjectDirectory"))
      .map(new File(_)).toSet

  protected override def isTest(environment: Environment): Boolean =
    environment.javaCommand.contains("/target/surefire/surefirebooter") &&
    environment.isPropertyPresent("surefire.test.class.path") &&
    environment.isPropertyPresent("surefire.real.class.path") &&
    environment.isPropertyPresent("localRepository") &&
    environment.isPropertyPresent("basedir")

  protected override def isAppMain(environment: Environment): Boolean =
    environment.javaCommand.startsWith("org.codehaus.plexus.classworlds.launcher.Launcher") &&
// TODO    environment.classPath.exists(_.getAbsolutePath.contains("plexus-classworlds")) &&
    environment.isPropertyPresent("classworlds.conf") &&
    environment.isPropertyPresent("maven.home") &&
    environment.isPropertyPresent("maven.multiModuleProjectDirectory")

  protected override def isDebug(environment: Environment): Boolean = false
}
