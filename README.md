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
    case class AccountId(id: Long) extends AnyVal
    
    case class Person(id: PersonId, name: String)
    case class Account(id: AccountId, name: String)
    
    def getPerson(id: PersonId): Person
    def getAccount(id: Account): Account

Although both `Person`s and `Account`s are identified by `Long`s, by wrapping those `Long` values
in different value classes we can be sure that we can't accidentally pass an identifier to a `Person`
to the `getAccount` function without the compiler telling us we've done the wrong thing.

## Value classes can cause some unwanted friction

As useful as value classes are, there are a lot of situations where wrapping and unwrapping the values
can cause enough friction in your code that you wonder if they're worth the effort. For example, if
you render the `Person` class defined above to JSON using Play Framework's very handy macro-generated
`Writes` instances you end up with an undesirable extra level of structure in the output, e.g.

```
    {
        "id": {
            "id": 1
        },
        "name": "Fred"
    }
```

when we'd rather just have

```
    {
        "id": 1,
        "name": "Fred"
    }
```

To get around this you end up having to write your own `Writes`, and associated `Reads` implementation
to deal with the extra structure. This quickly adds up to a lot of annoying boilerplate. The purpose
of the `value-wrapper` library is to eliminate that boilerplate. By providing instances of `ValueWrapper`
for a given wrapper and wrapped type we can build generic versions of things like `Reads` and `Writes` that
work for all value classes.

## Going fully generic with Shapeless

But building instances of `ValueWrapper` for our value classes still leaves us with boilerplate like this:

```
implicit val personIdWrapper = new ValueWrapper[PersonId, Long]  {
  def wrap(l: Long) : Either[String, PersonId] = Right(PersonId(l))  // or could do validation here
  def unwrap(p: PersonId): Long = p.id
}
```

This is less boilerplate than before and the typeclass instance can be used to construct multiple different
kinds of other typeclasses, e.g. Play's `QueryStringBindable`s and `PathBindable`s, but given that the
implementation is pretty much the same for all single-value case classes, wouldn't it be great if we could
eliminate it altogether? Turns out we can, with a small dash of Shapeless.
 
The `ValueWrapperGen` trait provided in this library provides an implicit function that tells the compiler
how to generate a `ValueWrapper` instance for any single-value case class (and, in fact, any class that
Shapeless considers to look enough like a single-value case class that it can build an appropriate `Generic` 
instance for). Extend `ValueWrapperGen` or import `ValueWrapperGen._` to bring that implicit function
into scope and any other function you have that defines an implicit parameter of type `ValueWrapper[W, V]`
will get provided with a compiler-generated instance with no additional code!
 
So now you can write something like this:

```
trait ValueClassWrites extends ValueWrapperGen {
  implicit def genericWrites[W, V](implicit vw: ValueWrapper[W, V],
                                   wv: Writes[V]): Writes[W] =
    new Writes[W] {
      override def writes(w: W): JsValue = wv.writes(vw.unwrap(w))
    }
}
```

and use that implicit function to generate a `Writes` instance for any value class. In fact, this is
exactly what the [`play-bindings`](https://github.com/WellFactored/play-bindings) library provides, as well as generic instance constructors for `Reads`, 
`PathBindable` and `QueryStringBindable`.

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
 allows us to define different validations applied to a primitive type, say `Long` depending on the type it is going
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
