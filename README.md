[![CircleCI](https://circleci.com/gh/WellFactored/value-wrapper.svg?style=svg)](https://circleci.com/gh/WellFactored/value-wrapper)
[![Stories in Ready](https://badge.waffle.io/WellFactored/value-wrapper.png?label=ready&title=Ready)](https://waffle.io/WellFactored/value-wrapper)
[![Codacy Badge](https://api.codacy.com/project/badge/Grade/bbd834e020d74efabee786d768c2d609)](https://www.codacy.com/app/doug/value-wrapper?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=WellFactored/value-wrapper&amp;utm_campaign=Badge_Grade)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.wellfactored/value-wrapper_2.11/badge.png)](https://maven-badges.herokuapp.com/maven-central/com.wellfactored/value-wrapper_2.11)

# value-wrapper

## A micro-library to provide a convenient way of wrapping and unwrapping scala value classes 
 
A value class is a case class, of type `W`, that wraps a single value, of type `V` and optionally 
extends `AnyVal`. Value classes are a really convenient and lightweight way of strongly typing 
primitive values to avoid passing them to functions incorrectly.

For example:

    case class PersonId(id: Long) extends AnyVal
    case class Person(id: PersonId, name: String)


## Validation and Normalisation of values

Sometimes you want to validate the value before allowing the value class to be created. `value-wrapper`
provides a type class called `Validator[W, V]` that will let you do this. For example, we might want to ensure
that the `Long` being used to construct a `UserId` must be non-negative. We could provide an instance of
`Validator[W, V]` that looks like this:

```
implicit val vl = new Validator[UserId, Long] {
  override def validate(l: Long): Either[String, Long] = if (l >= 0) Right(l) else Left(s"Id must be non-negative ($l)")
}
```

If this validator is in implicit scope at the point where the compiler is instantiating a `ValueWrapper` 
for `UserId` then it will get picked up and used as part of the code that constructs the instance of `W`.
 
 Looking at the declaration of `validate` there are a couple of things to note:
 
 * If validation is successful then it returns a `Right[V]`, not a `Right[W]`. The purpose of `validate` is to
 validate the value of type `V` _in the context of_ the type `W`, not to construct the instance of `W`. This
 allows us to have different validation applied to a primitive type, say `Long` depending on the type it is going
 to be wrapped in.
 * The value wrapped in the `Right[V]` is the value that will be used by the binders to construct the instance of
 `W`. This gives us the chance to change the value as part of the validation. For example, we might want to 
 normalise strings by stripping whitespace like this:
 
 ```
 case class Foo(s:String)
 
 implicit val vl = new Validator[Foo, String] {
   override def validate(s:String): Either[String, String] = Right(s.trim)
 }
 ```
