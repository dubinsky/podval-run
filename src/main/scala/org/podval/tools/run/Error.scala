package org.podval.tools.run

case class Error(message: String)

object Error {
  type Or[T] = Either[Error, T]
  type OrOption[T] = Either[Error, Option[T]]

  def atMostOne[T](what: String, things: Iterable[T]): OrOption[T] = {
    if (things.size > 1) {
      val list: String = things.mkString(", ")
      Left(Error(s"More than one $what: $list"))
    } else
      Right(things.headOption)
  }
}
