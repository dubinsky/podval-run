package org.podval.tools.run

import java.io.File

case class Environment private(
  javaCommand: String,
  directories: Set[File],
  jars: Set[File],
  jarDirectories: Set[File],
  explodedWars: Set[File],
  wars: Set[File],
  classPathOther: Set[File],
  properties: Map[String, String]
) {
  def isJarPresent(what: String): Boolean = jars.exists(_.getAbsolutePath.endsWith(what + ".jar"))

  def isPropertyPresent(name: String): Boolean = properties.contains(name)

  def propertyContains(name: String, what: String): Boolean = properties.get(name).exists(_.contains(what))

  override def toString: String = {
    def paths(name: String, values: Set[File]): String =
      s"$name\n" + values.map(value => s"  $value\n").mkString("")

    s"Java command: $javaCommand\n" +
    paths("Directories:", directories) +
    paths("JARs:", jars) +
    paths("JAR directories:", jarDirectories) +
    paths("Exploded WARs:", explodedWars) +
    paths("WARs:", wars) +
    paths("Classpath other:", classPathOther) +
    s"Properties:\n" + (for { (name, value) <- properties } yield s"  $name=$value\n").mkString("")
  }
}

object Environment {
  import java.net.URLClassLoader
  import scala.collection.JavaConverters._
  import Util.dropSuffix

  private val ignoredPrefixes: Set[String] = Set("java", "sun", "user", "os", "file", "path", "line", "awt")

  def get: Environment = {
    def getProperty(name: String): String = Option(System.getProperty(name)).get

    val javaCommand: String = getProperty("sun.java.command")

    val classLoaderClassPath: Seq[String] = getClass.getClassLoader match {
      case cl: URLClassLoader => cl.getURLs.toSeq.map(_.getPath) // TODO look only at the 'file:' URLs?
      case _ =>  Seq.empty
    }

    val javaClassPath: Seq[String] = getProperty("java.class.path").split(':')

    val classPath: Set[File] = (classLoaderClassPath ++ javaClassPath)
      .map(path => if (path.endsWith("/")) path.init else path)
      .filterNot(_.contains("/jre/lib"))
      .map(path => new File(path))
      .filter(_.exists)
      .toSet

    val wars: Set[File] = classPath.filterNot(_.isDirectory).filter(_.getAbsolutePath.endsWith(".war"))
    val jars: Set[File] = classPath.filterNot(_.isDirectory).filter(_.getAbsolutePath.endsWith(".jar"))
    val jarDirectories: Set[File] = jars.map(_.getParentFile).filter(_.isDirectory)
    val directories: Set[File] = classPath.filter(_.isDirectory)
    val explodedWars: Set[File] = directories.flatMap { directory =>
      dropSuffix(directory, "/WEB-INF/classes").orElse(dropSuffix(directory, "/WEB-INF/lib"))
    }
    val classPathOther: Set[File] = classPath -- directories -- jars -- wars

    val properties: Map[String, String] = System.getProperties
      .asScala
      .toMap
      .filterKeys(name => !ignoredPrefixes.exists(prefix => name.startsWith(prefix + ".")))

    new Environment(
      javaCommand = javaCommand,
      directories = directories,
      jars = jars,
      jarDirectories = jarDirectories,
      explodedWars = explodedWars,
      wars = wars,
      classPathOther = classPathOther,
      properties = properties
    )
  }
}
