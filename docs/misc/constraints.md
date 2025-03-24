### Additional constraint traits

The constraints described in this document are meant to extend the set of constraints and behavioural traits provided by the smithy standard library (such as `smithy.api#pattern` or `smithy.api#length`).

Users defining new protocols/tools MAY use to implement support for these traits. In particular, [smithy-translate](https://github.com/disneystreaming/smithy-translate) makes use of these traits to capture in smithy some semantics described in openapi.

#### alloy#dateFormat

This trait is used to express that a `String` in your model is formatted as a date. The format is defined in the [RFC 3339](https://www.rfc-editor.org/rfc/rfc3339#section-5.6). Example: `2022-12-28`.

```smithy
structure Test {
  @dateFormat
  myDate: String
}
```

#### alloy#nullable

Out of the box, Smithy does not make a distinction between a missing value and a value set to `null`. Some other Interface Definition Languages (IDL) allow for this distinction. This trait can be used to express this distinction.

```smithy
structure Foo {
 @required
 @nullable
 bar: String
}
```

#### alloy#defaultValue

Smithy 2.0 introduces the [`@default` trait](https://smithy.io/2.0/spec/type-refinement-traits.html#default-trait) but this trait is restrictive and can't be used in some use case. For example, you can use `@defaultValue` to set a default of `"N/A"` on a `String` that's constrained with the `length` trait to a minimum of 5 characters. Smithy's `@default` trait won't allow that.

```smithy
@length(min: 5)
string MyString

structure Foo {
 @required
 @defaultValue("N/A")
 bar: String
}
```


### alloy#openEnum

Specifies that an enumeration is open meaning that it can accept "unknown" values that are not explicitly specified inside of the smithy enum shape definition.
This trait should be mainly be used for interop with external libraries that require it. Often a string or integer type may be more applicable if there are many different
possible values that the API can return.

This trait can be applied to `enum` or `intEnum` shapes. Additionally it can be used on String shapes with the `smithy.api#enum` trait. This is supported for backward compatibility since the `enum` constraint trait is deprecated.

```smithy
@openEnum
enum Shape {
  SQUARE, CIRCLE
}

@openEnum
intEnum IntShape {
  SQUARE = 1
  CIRCLE = 2
}
```

### alloy#structurePattern

The `alloy#structurePattern` trait provides a way to specify that a given `String` will conform to a provided format and that it should be parsed into a `Structure` rather than a `String`. For example:

```smithy
@structurePattern(pattern: "{foo}_{bar}", target: FooBar)
string FooBarString

structure FooBar {
  @required
  foo: String
  @required
  bar: Integer
}
```

Now wherever `FooBarString` is used, it will really be parsing the string into the structure `FooBar`. There are a few requirements for using the `structurePattern` trait that are checked by a validator:

- The target structure must have all required members and all members must target simple shapes.
- The provided pattern must have all parameters separated by at least one character. The reason for this is that if there is no separation (e.g. "{foo}{bar}") then a parser would not be able to tell when one starts and the other begins.
- There must be a provided pattern parameter for each member of the target structure.
