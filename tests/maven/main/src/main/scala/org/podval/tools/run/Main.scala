package org.podval.tools.run

object Main {
  def main(args: Array[String]): Unit =
    println(Run.get.fold(_.toString, _.toString))
}
