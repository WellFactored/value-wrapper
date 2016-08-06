package com.wellfactored.valuewrapper

import cats.data.Validated
import cats.syntax.validated._

/**
  * Defines a validation of values of type `V` in the context of another type `W`, for example
  * if `W` instance wrap values of type `V`
  *
  * So if you have `case class UserId(id: Long)` you might define an implicit `Validator[UserId, Long]`
  * that checks that the `Long` value is non-negative.
  *
  * @tparam W The context in which the validation takes place
  * @tparam V The type of the values that will be validated
  */
trait Validator[W, V] {
  /**
    * Check that the value v is valid in the context of the type W. If it is then
    * return a `Right[V]` containing the value. In addition to validation, this provides
    * an opportunity to normalize the incoming value in some way for the returned value.
    *
    * If the value is not valid then return a `Left[String]` containing an error message.
    *
    * @param v The value to be validated and/or normalized
    * @return `Right[V]` if `v` is valid, or `Left[String]` with an error message if `v`
    *         is invalid
    */
  def validate(v: V): Validated[String, V]
}

trait ValidatorLowPriorityImplicits {
  /**
    * Define a low-priority instance that allows any value of type `V`. When using ValueWrapper
    * this instance will get picked up unless you define your own implicit instance to override it.
    */
  implicit def identityValidator[W, V]: Validator[W, V] = new Validator[W, V] {
    override def validate(a: V): Validated[String, V] = a.valid
  }
}

/**
  * Pull the low-priority instances into the companion object so they'll be available in
  * the implicit scope.
  */
object Validator extends ValidatorLowPriorityImplicits
