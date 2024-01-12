<!-- Using `yzhang.markdown-all-in-one` VS Code extension to create the table of contents -->
# Alloy <!-- omit in toc -->

A collection of commonly used Smithy shapes.

## Table of Contents <!-- omit in toc -->

- [Using Alloy](#using-alloy)
- [Why Alloy?](#why-alloy)
- [Included Shapes](#included-shapes)
  - [alloy#dateFormat](#alloydateformat)
  - [alloy#nullable](#alloynullable)
  - [alloy#defaultValue](#alloydefaultvalue)
  - [alloy#dataExamples](#alloydataexamples)
  - [alloy#openEnum](#alloyopenenum)
  - [alloy#structurePattern](#alloystructurepattern)
  - [alloy.openapi](#alloyopenapi)
    - [alloy.openapi#openapiExtensions](#alloyopenapiopenapiextensions)
- [Protocol Compliance Module](#protocol-compliance-module)
  - [Using the Protocol Compliance Tests](#using-the-protocol-compliance-tests)
- [Working on Alloy](#working-on-alloy)
  - [Publish Local](#publish-local)
  - [Run Tests](#run-tests)

## Using Alloy

Alloy Smithy shapes and validators are published to Maven Central under the following artifact names:

For sbt:

```scala
"com.disneystreaming.alloy" % "alloy-core" % "x.x.x"
"com.disneystreaming.alloy" %% "alloy-openapi" % "x.x.x"
```

For mill:

```scala
ivy"com.disneystreaming.alloy:alloy-core:x.x.x"
ivy"com.disneystreaming.alloy::alloy-openapi:x.x.x"
```

## Why Alloy?

Alloy was created to unify the Smithy shapes that we use across our projects, including for example `smithy4s` and `smithy-translate`. Having the shapes defined in one spot means that we can use them everywhere and our tooling will interop seamlessly.

## Included Shapes

Alloy currently includes shapes related to the following two protocols:

- `alloy#simpleRestJson`
- `alloy#grpc`

That being said, you can use the shapes in Alloy without using these protocols if you want to customize your protocol differently from what we have defined here.



### alloy#dateFormat

This trait is used to express that a `String` in your model is formatted as a date. The format is defined in the [RFC 3339](https://www.rfc-editor.org/rfc/rfc3339#section-5.6). Example: `2022-12-28`.

```smithy
structure Test {
  @dateFormat
  myDate: String
}
```

### alloy#nullable

Smithy does not make a distinction between a missing value and `null` but some Interface Definition Languages (IDL) can. This trait can be used to express this distinction.

```smithy
structure Foo {
 @required
 @nullable
 bar: String
}
```

### alloy#defaultValue

Smithy 2.0 introduces the [`@default` trait](https://smithy.io/2.0/spec/type-refinement-traits.html#default-trait) but this trait is restrictive and can't be used in some use case. For example, you can use `@defaultValue` to set a default of `"N/A"` on a `String` that's constrained with the `length` trait to a minimum of 5 characters. Smithy's `@default` trait won't allow that.

```smithy
@length(min: 5)
string MyString

structure Foo {
 @required
 @nullable
 bar: String
}
```

### alloy#dataExamples

This trait allows you to provide concrete examples of what instances of a given shape will look like. There are three different formats that examples can be provided in: smithy, json, or string.

- Smithy format: Here you will define your examples to match the format of the shape the trait is on. The format must match exactly, and must not include protocol specific information such as `jsonName`. A validator is run on this format type to make sure the data matches the shape it is on.
- Json format: This format is similar to the smithy one, except you can put anything you want in the contents of the JSON. This means you can include protocol-specific pieces such as taking into account the `jsonName` trait.
- String format: This is just a string that is not validated. This format is most useful for protocols that do not support JSON.

```smithy
@dataExamples([
  {
    smithy: {
      name: "Emily",
      age: 64
    }
  },
  {
    json: {
      fullName: "Allison",
      age: 22
    }
  },
  {
    string: "{ fullName: \"Sarah\" }"
  }
])
structure User {
    @jsonName("fullName")
    name: String
    age: Integer
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
- There must be a provided pattern parameter for each member of the structure.

### alloy.openapi

This namespace contains shapes related to the OpenAPI format. These shapes can be used to express OpenAPI specification details that do not translate naturally in Smithy.

#### alloy.openapi#openapiExtensions

OpenAPI has support for [extensions](https://swagger.io/docs/specification/openapi-extensions). You can use this trait to reflect that in your Smithy specification:

```smithy
@openapiExtensions(
  "x-foo": "bar"
)
list StringList {
  member: String
}
```

## Protocol Compliance Module
 - Alloy contains a suite of protocol tests utilizing the [AWS HTTP Protocol Compliance Test Module]("https://smithy.io/2.0/additional-specs/http-protocol-compliance-tests.html)
 - These can be used to test an implementation of the simpleRestJson protocol to confirm compliance with the protocol .
   For sbt:

### Using the Protocol Compliance Tests
```scala
"com.disneystreaming.alloy" % "alloy-protocol-tests" % "x.x.x"
```

For mill:

```scala
ivy"com.disneystreaming.alloy:alloy-protocol-tests:x.x.x"
```

## Working on Alloy

### Publish Local

```console
> ./mill __.publishLocal
```

### Run Tests

```console
> ./mill __.test
```
