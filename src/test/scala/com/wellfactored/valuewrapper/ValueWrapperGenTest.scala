package com.wellfactored.valuewrapper

import org.scalatest.{Matchers, WordSpecLike}


trait Writes[V] {
  def write(v: V): String
}

object Writes {
  implicit val stringWrites = new Writes[String] {
    override def write(s: String): String = s
  }
}

trait ValueClassWrites extends ValueWrapperGen {
  implicit def genericWrites[W, V](implicit vw: ValueWrapper[W, V],
                                   wv: Writes[V]): Writes[W] =
    new Writes[W] {
      override def write(w: W): String = wv.write(vw.unwrap(w))
    }
}

class ValueWrapperGenTest extends WordSpecLike with Matchers with TestWrappers with ValueClassWrites {
  "An implicitly summoned ValueWrapper for StringWrapper" should {
    val wrapper = implicitly[ValueWrapper[StringWrapper, String]]

    "wrap a string" in {
      wrapper.wrap("Test") shouldBe Right(StringWrapper("Test"))
    }

    "unwrap a string" in {
      wrapper.unwrap(StringWrapper("Test")) shouldBe "Test"
    }
  }

  "An implicitly summoned Writes" should {
    "summon a ValueWrapper" in {
      val writes = implicitly[Writes[StringWrapper]]

      writes.write(StringWrapper("s")) shouldBe "s"
    }
  }
}
