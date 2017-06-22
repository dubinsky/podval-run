package org.podval.tools.run

import org.junit.{Assert, Test}

final class RunTest {

  @Test def test(): Unit = {
    val result = Run.get
    println(result.fold(_.toString, _.toString))
    Assert.assertTrue(result.right.get.isTest)
  }
}
