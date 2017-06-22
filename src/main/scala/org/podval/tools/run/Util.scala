package org.podval.tools.run

import java.io.File

object Util {
  def dropSuffix(directory: File, suffix: String): Option[File] = {
    if (!directory.getAbsolutePath.endsWith(suffix)) None else {
      val numberOfLevels: Int = suffix.split('/').count(!_.isEmpty)
      var result: File = directory
      for (_ <- 1 to numberOfLevels) result = result.getParentFile
      Some(result)
    }
  }

  def hasFile(directory: File, name: String): Boolean = directory.isDirectory && new File(directory, name).exists()

  def ancestors(file: File): Seq[File] = Util.unfold1(file)(file => Option(file.getParentFile))

  def unfold1[A](seed: A)(step: A => Option[A]): Seq[A] = {
    def step1(seed: A): Option[(A, A)] = step(seed).fold[Option[(A, A)]](None)(r => Some(r, r))
    Seq(seed) ++ unfold(seed)(step1)
  }

  def unfold[B, A](seed: B)(step: B => Option[(A, B)]): Seq[A] = {
    step(seed).fold(Seq.empty[A]) { case (element, nextSeed) => Seq(element) ++ unfold(nextSeed)(step) }
  }
}
