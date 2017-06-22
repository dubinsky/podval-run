package org.podval.tools.run

object Tomcat extends WebApp.Server {
  override def toString: String = "Tomcat"

  override def isPresent(environment: Environment): Boolean = {
    environment.javaCommand.startsWith("org.apache.catalina.startup.Bootstrap start") &&
    environment.isJarPresent("/bin/bootstrap") &&
    environment.isJarPresent("/bin/tomcat-juli") &&
    environment.isPropertyPresent("catalina.home") &&
    environment.isPropertyPresent("catalina.base")
    //   catalina.useNaming=true
    //   org.apache.catalina.startup.ContextConfig.jarsToSkip=
    //   java.naming.factory.initial=org.apache.naming.java.javaURLContextFactory
    //   java.naming.factory.url.pkgs=org.apache.naming
    //   java.util.logging.manager=org.apache.juli.ClassLoaderLogManager
    //   tomcat.util.scan.DefaultJarScanner.jarsToSkip=...
    //   org.apache.catalina.startup.TldConfig.jarsToSkip=...
    //   package.definition=...
  }
}
