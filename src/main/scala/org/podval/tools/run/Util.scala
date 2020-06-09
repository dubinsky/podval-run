package org.podval.tools.run

import java.io.File

object Util {

  def recognizeOneOf(directory: File, suffixes: Seq[Seq[String]]): Option[File] =
    if (suffixes.isEmpty) None
    else recognize(directory, suffixes.head).orElse(recognizeOneOf(directory, suffixes.tail))

  @scala.annotation.tailrec
  def recognize(directory: File, suffix: Seq[String]): Option[File] =
    if (suffix.isEmpty) Some(directory)
    else if (recognize(directory.getName, suffix.last)) recognize(directory.getParentFile, suffix.init)
    else None

  def recognize(directoryName: String, pattern: String): Boolean =
    (pattern == "*") || (pattern == directoryName)

  def hasFile(directory: File, name: String): Boolean = directory.isDirectory && new File(directory, name).exists()

  def ancestors(file: File): Seq[File] = Util.unfold1(file)(file => Option(file.getParentFile))

  def unfold1[A](seed: A)(step: A => Option[A]): Seq[A] = {
    def step1(seed: A): Option[(A, A)] = step(seed).fold[Option[(A, A)]](None)(r => Some(r, r))
    Seq(seed) ++ unfold(seed)(step1)
  }

  def unfold[B, A](seed: B)(step: B => Option[(A, B)]): Seq[A] =
    step(seed).fold(Seq.empty[A]) { case (element, nextSeed) => Seq(element) ++ unfold(nextSeed)(step) }
}
