package com.wellfactored.valuewrapper

import shapeless.{::, Generic, HNil, Lazy}

/**
  * This trait provides an implicit function that will generate a ValueWrapper[W,V]
  * for a case class of type `W` that has a single member of type `V`. This uses
  * Shapeless to summon a `Generic[W, V :: HNil]` to assist with the wrapping
  * and unwrapping, so it will actually work with any type `W` that is record-like
  * enough for Shapeless to handle.
  *
  * The `genWV` function also takes an implicit `Validator[W,V]` that allows for
  * some form of validation and manipulation of the value to be wrapped when
  * constructing the `W` instance.
  *
  */
trait ValueWrapperGen {
  /**
    *
    * @param gen provides the Generic mapping between the wrapper type and the wrapped
    *            value type. Using a `Repr` type of `V :: HNil` proves that `W` wraps
    *            a single value of type `V`
    * @param vl  a `Validator` instance that will validate a value of type `V` in the context
    *            of a wrapper of type `W`. The `Validator` object provides a low-priority instance
    *            which is an identity function (i.e. always validates successfully), which will
    *            get picked up if you do not provide a higher-priority instance yourself.
    */
  implicit def genWV[W, V](implicit gen: Lazy[Generic.Aux[W, V :: HNil]],
                           vl: Validator[W, V]): ValueWrapper[W, V] =
  new ValueWrapper[W, V] {
    override def wrap(v: V): Either[String, W] =
      vl.validate(v).map(v2 => gen.value.from(v2 :: HNil)).toEither

    override def unwrap(w: W): V =
      gen.value.to(w).head
  }
}
