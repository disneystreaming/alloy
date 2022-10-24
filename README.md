<!-- Using `yzhang.markdown-all-in-one` VS Code extension to create the table of contents -->
# Alloy <!-- omit in toc -->

A collection of commonly used Smithy shapes.

## Table of Contents <!-- omit in toc -->

- [Using Alloy](#using-alloy)
- [Why Alloy?](#why-alloy)
- [Included Shapes](#included-shapes)
  - [alloy#simpleRestJson](#alloysimplerestjson)
    - [Unions](#unions)
      - [Tagged union](#tagged-union)
    - [Untagged union](#untagged-union)
    - [Discriminated union](#discriminated-union)
    - [Full List of Supported Traits](#full-list-of-supported-traits)
  - [alloy#grpc](#alloygrpc)
    - [alloy#protoIndex](#alloyprotoindex)
    - [alloy#protoNumType](#alloyprotonumtype)
    - [alloy#protoEnabled](#alloyprotoenabled)
    - [alloy#protoReservedFields](#alloyprotoreservedfields)
- [Working on Alloy](#working-on-alloy)
  - [Publish Local](#publish-local)
  - [Run Tests](#run-tests)

## Using Alloy

Alloy Smithy shapes and validators are published to Maven Central under the following artifact names:

```
"com.disneystreaming.alloy:alloy-core:x.x.x"
"com.disneystreaming.alloy:alloy-openapi:x.x.x"
```

## Why Alloy?

Alloy was created to unify the Smithy shapes that we use across our projects, including for example `smithy4s` and `smithy-translate`. Having the shapes defined in one spot means that we can use them everywhere and our tooling will interop seamlessly.

## Included Shapes

Alloy currently includes shapes related to the following two protocols:

- `alloy#simpleRestJson`
- `alloy#grpc`

That being said, you can use the shapes in Alloy without using these protocols if you want to customize your protocol differently from what we have defined here.

### alloy#simpleRestJson

This is the protocol that was formerly known as `smithy4s.api#simpleRestJson`.

This protocol is aware of the following `smithy.api` traits provided out of the box:

* [all simple shapes](https://awslabs.github.io/smithy/1.0/spec/core/model.html#simple-shapes)
* composite data shapes, including collections, unions, structures.
* [operations and services](https://awslabs.github.io/smithy/1.0/spec/core/model.html#service)
* [enumerations](https://awslabs.github.io/smithy/1.0/spec/core/constraint-traits.html#enum-trait)
* [error trait](https://awslabs.github.io/smithy/1.0/spec/core/type-refinement-traits.html#error-trait)
* [http traits](https://awslabs.github.io/smithy/1.0/spec/core/http-traits.html), including **http**, **httpError**, **httpLabel**, **httpHeader**, **httpPayload**, **httpQuery**, **httpPrefixHeaders**, **httpQueryParams**.
* [timestampFormat trait](https://awslabs.github.io/smithy/1.0/spec/core/protocol-traits.html?highlight=timestampformat#timestampformat-trait)

Further, it contains several traits for customizing your APIs.

#### Unions

Unions in this protocol can be encoded in three different ways: tagged, discriminated, and untagged.

By default, the specification of the Smithy language hints that the `tagged-union` encoding should be used. This is arguably the best encoding for unions, as it works with members of any type (not just structures), and does not require backtracking during parsing, which makes it more efficient.

However, `alloy#simpleRestJson` supports two additional encodings: `discriminated` and `untagged`, which users can opt-in via the `alloy#discriminated` and `alloy#untagged` trait, respectively. These are mostly offered as a way to retrofit existing APIs in Smithy.


##### Tagged union

This is the default behavior, and happens to visually match how Smithy unions are declared. In this encoding, the union is encoded as a JSON object with a single key-value pair, the key signalling which alternative has been encoded.

```
union Tagged {
  first: String
  second: IntWrapper
}

structure IntWrapper {
  int: Integer
}
```

The following instances of `Tagged`

```scala
Tagged.FirstCase("alloy")
Tagged.SecondCase(IntWrapper(42)))
```

are encoded as such :

```json
{ "first": "alloy" }
{ "second": { "int": 42 } }
```

#### Untagged union

Untagged unions are supported via an annotation: `@untagged`. Despite the smaller payload size this encoding produces, it is arguably the worst way of encoding unions, as it may require backtracking multiple times on the parsing side. Use this carefully, preferably only when you need to retrofit an existing API into Smithy.

```kotlin
use alloy#untagged

@untagged
union Untagged {
  first: String
  second: IntWrapper
}

structure IntWrapper {
  int: Integer
}
```

The following instances of `Untagged`

```scala
Untagged.FirstCase("alloy")
Untagged.SecondCase(Two(42)))
```

are encoded as such :

```json
"alloy"
{ "int": 42 }
```

#### Discriminated union

Discriminated union are supported via an annotation: `@discriminated("tpe")`, and work only when all members of the union are structures.
In this encoding, the discriminator is inlined as a JSON field within JSON object resulting from the encoding of the member.

Despite the JSON payload exhibiting less nesting than in the `tagged union` encoding, this encoding often leads to bigger payloads, and requires backtracking once during parsing.

```kotlin
use alloy#discriminated

@discriminated("tpe")
union Discriminated {
  first: StringWrapper
  second: IntWrapper
}

structure StringWrapper {
  myString: String
}

structure IntWrapper {
  myInt: Integer
}
```

The following instances of `Discriminated`

```scala
Discriminated.FirstCase(StringWrapper("alloy"))
Discriminated.SecondCase(IntWrapper(42)))
```

are encoded as such

```json
{ "tpe": "first", "myString": "alloy" }
{ "tpe": "second", "myInt": 42 }
```

#### Full List of Supported Traits

- smithy.api#error
- smithy.api#required
- smithy.api#pattern
- smithy.api#range
- smithy.api#length
- smithy.api#http
- smithy.api#httpError
- smithy.api#httpHeader
- smithy.api#httpLabel
- smithy.api#httpPayload
- smithy.api#httpPrefixHeaders
- smithy.api#httpQuery
- smithy.api#httpQueryParams
- smithy.api#jsonName
- smithy.api#timestampFormat
- alloy#uncheckedExamples
- alloy#uuidFormat
- alloy#discriminated
- alloy#untagged

For full documentation on what each of these traits does, see the smithy specifications [here](modules/core/resources/META-INF/smithy/).

### alloy#grpc

This protocol represents the GRPC protocol as defined at [grpc.io](https://grpc.io/).

The following shapes are provided as a means of customizing how your Smithy shapes correlate to proto ones.

- alloy#grpc
- alloy#protoIndex
- alloy#protoNumType
- alloy#protoEnabled
- alloy#protoReservedFields
- alloy#uncheckedExamples

#### alloy#protoIndex

Marks an explicit index to be used for a structure member when it is
interpreted as protobuf. For example:
```
structure Test {
  str: String
}
```

Is equivalent to:

```
message Test {
  string str = 1;
}
```

Where the following:

```
structure Test {
  @protoIndex(2)
  str: String
}
```

Is equivalent to:

```
message Test {
  string str = 2;
}
```

#### alloy#protoNumType

Specifies the type of signing that should be used for integers and longs. Options are:

- SIGNED
- UNSIGNED
- FIXED
- FIXED_SIGNED

#### alloy#protoEnabled

This trait can be used to enable protobuf conversion on services or structures that are not a part of a
GRPC service. This is used, for example, by smithy-translate.

#### alloy#protoReservedFields

Marks certain field indexes as unusable by the smithy specification. For example, if a range is provided of
1 to 10 then the proto indexes for any fields in that structure must fall outside of that range. Ranges are inclusive.

For full documentation on what each of these traits does, see the smithy specification [here](modules/core/resources/META-INF/smithy/proto/proto.smithy).

## Working on Alloy

### Publish Local

```console
> mill __.publishLocal
```

### Run Tests

```console
> mill __.test
```
