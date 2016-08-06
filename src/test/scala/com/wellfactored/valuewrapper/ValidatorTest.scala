package com.wellfactored.valuewrapper

import org.scalatest.{Matchers, WordSpecLike}

class ValidatorTest extends WordSpecLike with Matchers with TestValidators {

  import ValueWrapperGen._

  "An implicitly summoned ValueWrapper for LongWrapper" should {
    "wrap -1 without validation" in {
      val wrapper = implicitly[ValueWrapper[LongWrapper, Long]]
      wrapper.wrap(-1) shouldBe Right(LongWrapper(-1))
    }

    "not wrap -1 when nonNegativeLong validator is in scope" in {
      implicit val validator = nonNegativeLong
      val wrapper = implicitly[ValueWrapper[LongWrapper, Long]]
      wrapper.wrap(-1) shouldBe 'left
    }
  }

  "An implicitly summoned ValueWrapper for StringWrapper" should {
    "convert the wrapped string to lower case when normaliseToLowerCase validator is in scope" in {
      implicit val validator = normaliseToLowerCase
      val wrapper = implicitly[ValueWrapper[StringWrapper, String]]
      wrapper.wrap("TEST") shouldBe Right(StringWrapper("test"))
    }
  }
}
