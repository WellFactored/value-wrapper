package com.wellfactored.valuewrapper

import org.scalatest.{Matchers, WordSpecLike}

class ValueWrapperGenTest extends WordSpecLike with Matchers with TestWrappers with ValueWrapperGen {
  "An implicitly summoned ValueWrapper for StringWrapper" should {
    val wrapper = implicitly[ValueWrapper[StringWrapper, String]]

    "wrap a string" in {
      wrapper.wrap("Test") shouldBe Right(StringWrapper("Test"))
    }

    "unwrap a string" in {
      wrapper.unwrap(StringWrapper("Test")) shouldBe "Test"
    }
  }
}
