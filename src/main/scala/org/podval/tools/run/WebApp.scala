package org.podval.tools.run

import java.io.File

sealed trait WebApp {
  def root: File
}

object WebApp {
  case class Inplace (override val root: File, module: File) extends WebApp
  case class Exploded(override val root: File) extends WebApp
  case class War     (override val root: File) extends WebApp

  trait Server {
    def isPresent(environment: Environment): Boolean
  }

  case class InServer(webApp: WebApp, server: Server) {
    override def toString: String = s"$webApp in $server"
  }
}
