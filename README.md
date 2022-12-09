<!-- Using `yzhang.markdown-all-in-one` VS Code extension to create the table of contents -->
# Alloy <!-- omit in toc -->

A collection of commonly used Smithy shapes.

## Table of Contents <!-- omit in toc -->

- [Using Alloy](#using-alloy)
- [Why Alloy?](#why-alloy)
- [Included Shapes](#included-shapes)
  - [alloy#simpleRestJson](#alloysimplerestjson)
    - [Protocol Behavior and Semantics](#protocol-behavior-and-semantics)
      - [Required Traits](#required-traits)
      - [Content-types](#content-types)
      - [JSON Shape Serialization](#json-shape-serialization)
      - [Http Bindings](#http-bindings)
      - [Operation Error Encoding](#operation-error-encoding)
    - [Supported Traits](#supported-traits)
    - [Unions](#unions)
      - [Tagged union](#tagged-union)
    - [Untagged union](#untagged-union)
    - [Discriminated union](#discriminated-union)
    - [Full List of Supported Traits](#full-list-of-supported-traits)
  - [alloy.proto#grpc](#alloyprotogrpc)
    - [alloy.proto#protoIndex](#alloyprotoprotoindex)
    - [alloy.proto#protoNumType](#alloyprotoprotonumtype)
    - [alloy.proto#protoEnabled](#alloyprotoprotoenabled)
    - [alloy.proto#protoReservedFields](#alloyprotoprotoreservedfields)
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

### alloy#simpleRestJson

This is the protocol that was formerly known as `smithy4s.api#simpleRestJson`.

#### Protocol Behavior and Semantics

##### Required Traits

All operations referenced by a `alloy#simpleRestJson` service must be annotated with the [http](https://awslabs.github.io/smithy/2.0/spec/http-bindings.html#http-trait) trait.

Errors referenced by any operation that's itself referenced by a `@simpleRestJson` service should be uniquely annotated by a status code (within the context of that operation), using the `@httpError` trait. It means that two errors referenced by a same operation cannot have the same `statusCode`. If several error shapes can be raised using a single `statusCode`, a `union` should be used to represent the alternatives.

##### Content-types

The `alloy#simpleRestJson` protocol uses a Content-Type of `application/json`.

##### JSON Shape Serialization

| Smithy type | traits                                     | Json format                                                                                                                                                                                                                                                                                                                                                                                                                                          | Example                                  |
| ----------- | ------------------------------------------ | ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | ---------------------------------------- |
| blob        |                                            | Json string value, base64 encoded                                                                                                                                                                                                                                                                                                                                                                                                                    | `ImhlbGxvIg==`                           |
| boolean     |                                            | Json boolean                                                                                                                                                                                                                                                                                                                                                                                                                                         | true                                     |
| byte        |                                            | Json number                                                                                                                                                                                                                                                                                                                                                                                                                                          | 1                                        |
| short       |                                            | Json number                                                                                                                                                                                                                                                                                                                                                                                                                                          | 1                                        |
| integer     |                                            | Json number                                                                                                                                                                                                                                                                                                                                                                                                                                          | 1                                        |
| long        |                                            | Json number                                                                                                                                                                                                                                                                                                                                                                                                                                          | 1                                        |
| float       |                                            | Json number                                                                                                                                                                                                                                                                                                                                                                                                                                          | 1.1                                      |
| double      |                                            | Json number                                                                                                                                                                                                                                                                                                                                                                                                                                          | 1.1                                      |
| bigDecimal  |                                            | Json number                                                                                                                                                                                                                                                                                                                                                                                                                                          | 111111                                   |
| bigInteger  |                                            | Json number                                                                                                                                                                                                                                                                                                                                                                                                                                          | 111111                                   |
| string      |                                            | Json string                                                                                                                                                                                                                                                                                                                                                                                                                                          | "hello"                                  |
| timestamp   | (none, or `@timestampFormat("date-time")`) | Json string, following the date-time section of [RFC3339, section 5.6](https://xml2rfc.tools.ietf.org/public/rfc/html/rfc3339.html#anchor14)                                                                                                                                                                                                                                                                                                         | 1985-04-12T23:20:50.52Z                  |
| timestamp   | `@timestampFormat("http-date")`            | Json string, following the `IMF-fixdate` section of [RFC 7231](https://datatracker.ietf.org/doc/html/rfc7231.html#section-7.1.1.1)                                                                                                                                                                                                                                                                                                                   | Sun, 02 Jan 2000 20:34:56.000 GMT        |
| timestamp   | `@timestamp`                               | Json number, following Unix-time semantics, with optional fractional precision                                                                                                                                                                                                                                                                                                                                                                       | 1515531081.1234                          |
| document    |                                            | Json value (arbitrary shape)                                                                                                                                                                                                                                                                                                                                                                                                                         | [{"a": "b"}]                             |
| list        |                                            | Json array                                                                                                                                                                                                                                                                                                                                                                                                                                           | [1,2,2,3]                                |
| set         |                                            | Json array (with unique values)                                                                                                                                                                                                                                                                                                                                                                                                                      | [1, 2, 3]                                |
| map         |                                            | Json object                                                                                                                                                                                                                                                                                                                                                                                                                                          | {"a" : 1, "b" : 2}                       |
| structure   |                                            | Json object. Each member of the structure translates to a Json property when the name of the property is the same as the member name, unless that member is annotated with the [jsonName](https://awslabs.github.io/smithy/2.0/spec/protocol-traits.html#jsonname-trait). Members that are not annotated with the `required` trait can be omitted, or set to `null` to indicate an absence of value. Otherwise, the property values must be set | {"int": 1, "str": "hello"}               |
| union     |                                            | Same as structures, except that only a single member can be set to a non-null value.                                                                                                                                                                                                                                                                                                                                                                 | {"foo": {"int": 1, "str": "hello" }      |
| union      | @discriminated("type")                     | Same as unions, except a discriminator field is included. This field specifies which branch of the union is included in the encoded JSON. All member shapes in a discriminated union must be structures.                                                                                                                                                                                                                                             | {"type": "foo","int": 1, "str": "hello"} |
| union      | @untagged                                  | Same as structure, but the encode/decoding logic does not know what to deserialize to. `untagged` is not recommended. It is the least efficient approach, it's available to support existing APIs.                                                                                                                                                                                                                                                   | {"int": 1, "str": "hello"}               |

##### Http Bindings

The `alloy#simpleRestJson` protocol supports all of the HTTP binding traits defined in smithy's [HTTP protocol
bindings specification](https://awslabs.github.io/smithy/2.0/spec/http-bindings.html).
The serialization formats and and behaviors described for each trait are supported as defined in the
`alloy#simpleRestJson` protocol.


##### Operation Error Encoding

Error responses in the `simpleRestJson` protocol are serialized identically to successful responses, with the caveat that the status code of the http response should match what is set by the `error` and `httpError` traits.


```smithy
@error("client")
structure InvalidInputError {
}

@error("server")
structure UnexpectedServerError {
}

@error("client")
@httpError(403)
structure UnauthorisedError {
}
```

In the example above, `InvalidInputError` should be accompanied by the `400` status code, `UnexpectedServer` should be accompanied by the `500` status code, and `UnauthorisedError` should be accompanied by the `403` status code.

Because multiple errors can be encoded with the same status code, services implementing this protocol should include an `X-Error-Type` header that can be used to discriminate between them. For example, the following error...

```smithy
@error("client")
@httpError(403)
structure UnauthorisedError {
}
```

...should be given the header `X-Error-Type` with a value of `UnauthorisedError`. Clients can use this value to discriminate and provide the correct error to drive needed logic. If this header is not provided, clients will need to make a best-effort assumption about what error is intended using the status code.

#### Supported Traits

This protocol is aware of the following `smithy.api` traits provided out of the box:

* [all simple shapes](https://awslabs.github.io/smithy/1.0/spec/core/model.html#simple-shapes)
* composite data shapes, including collections, unions, structures.
* [operations and services](https://awslabs.github.io/smithy/1.0/spec/core/model.html#service)
* [enumerations](https://awslabs.github.io/smithy/1.0/spec/core/constraint-traits.html#enum-trait)
* [error trait](https://awslabs.github.io/smithy/1.0/spec/core/type-refinement-traits.html#error-trait)
* [http traits](https://awslabs.github.io/smithy/1.0/spec/core/http-traits.html), including **http**, **httpError**, **httpLabel**, **httpHeader**, **httpPayload**, **httpQuery**, **httpPrefixHeaders**, **httpQueryParams**.
* [timestampFormat trait](https://awslabs.github.io/smithy/1.0/spec/core/protocol-traits.html?highlight=timestampformat#timestampformat-trait)

Furthermore, it contains several traits for customizing your APIs.

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

### alloy.proto#grpc

This protocol represents the GRPC protocol as defined at [grpc.io](https://grpc.io/).

The following shapes are provided as a means of customizing how your Smithy shapes correlate to proto ones.

- alloy.proto#grpc
- alloy.proto#protoIndex
- alloy.proto#protoNumType
- alloy.proto#protoEnabled
- alloy.proto#protoReservedFields
- alloy.proto#uncheckedExamples

#### alloy.proto#protoIndex

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

When one field is annotated with a `@protoIndex`, all fields have to be annotated with it. This includes the fields of any structure used within the structure.

#### alloy.proto#protoNumType

Specifies the type of signing that should be used for integers and longs. Options are:

- SIGNED
- UNSIGNED
- FIXED
- FIXED_SIGNED

#### alloy.proto#protoEnabled

This trait can be used to enable protobuf conversion on services or structures that are not a part of a
GRPC service. This is used, for example, by smithy-translate.

#### alloy.proto#protoReservedFields

Marks certain field indexes as unusable by the smithy specification. For example, if a range is provided of
1 to 10 then the proto indexes for any fields in that structure must fall outside of that range. Ranges are inclusive.

For full documentation on what each of these traits does, see the smithy specification [here](modules/core/resources/META-INF/smithy/proto/proto.smithy).

## Working on Alloy

### Publish Local

```console
> ./mill __.publishLocal
```

### Run Tests

```console
> ./mill __.test
```
