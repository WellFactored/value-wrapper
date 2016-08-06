package com.wellfactored.valuewrapper

import cats.data.Validated
import cats.syntax.validated._

trait TestValidators extends TestWrappers {

  val normaliseToLowerCase = new Validator[StringWrapper, String] {
    override def validate(s: String): Validated[String, String] = s.toLowerCase.valid
  }

  val nonNegativeLong = new Validator[LongWrapper, Long] {
    override def validate(l: Long): Validated[String, Long] =
      if (l >= 0) l.valid else s"Id must be a non-negative integer ($l)".invalid
  }
}
