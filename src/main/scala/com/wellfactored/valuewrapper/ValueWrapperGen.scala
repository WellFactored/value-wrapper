package com.wellfactored.valuewrapper

import shapeless.{::, Generic, HList, HNil}

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
    *            value type.
    * @param ev1 provided evidence that the `Repr` in then `Generic` instance is an `HList`
    *            with one element of type `V`
    * @param ev2 Provides the reverse equivalence evidence between `Repr` and `V :: HNil`.
    *            `ev1` allows us to convert from `V` to `W` using `gen.from`, but we need `ev2`
    *            in order to be able to use `gen.to` to convert the other way, because `=:=` only
    *            proves type equivalence to the compiler in one direction.
    *            See http://typelevel.org/blog/2014/07/02/type_equality_to_leibniz.html
    */
  implicit def genGen[W, V, Repr <: HList](
                                            implicit
                                            gen: Generic.Aux[W, Repr],
                                            ev1: (V :: HNil) =:= Repr,
                                            ev2: Repr =:= (V :: HNil)
                                          ): Generic.Aux[W, V] = {
    new Generic[W] {
      override type Repr = V

      override def to(w: W) = gen.to(w).head

      override def from(v: V) = gen.from(v :: HNil)
    }
  }

  /**
    *
    * @param gen provides the Generic mapping between the wrapper type and the wrapped
    *            value type with optional validation of of V when wrapping.
    * @param vl  a `Validator` instance that will validate a value of type `V` in the context
    *            of a wrapper of type `W`. The `Validator` object provides a low-priority instance
    *            which is an identity function (i.e. always validates successfully), which will
    *            get picked up if you do not provide a higher-priority instance yourself.
    */
  implicit def genWV[W, V](
                             implicit
                             gen: Generic.Aux[W, V],
                             vl: Validator[W, V]
                           ): ValueWrapper[W, V] =
    new ValueWrapper[W, V] {
      override def wrap(v: V): Either[String, W] =
        vl.validate(v).map(v2 => gen.from(v2)).toEither

      override def unwrap(w: W): V = gen.to(w)
    }
}

/**
  * Import `ValueWrapperGen._` in cases where you can't or don't want to extend the
  * `ValueWrapperGen` trait yourself.
  */
object ValueWrapperGen extends ValueWrapperGen