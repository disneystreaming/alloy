### alloy#simpleRestJson

The `alloy#simpleRestJson` protocol is a REST protocol in which all HTTP bodies are serialised in JSON (top-level strings and binary blobs).

- smithy.api#default
- smithy.api#error
- smithy.api#http
- smithy.api#httpError
- smithy.api#httpHeader
- smithy.api#httpLabel
- smithy.api#httpPayload
- smithy.api#httpPrefixHeaders
- smithy.api#httpQuery
- smithy.api#httpQueryParams
- smithy.api#httpResponseCode
- smithy.api#jsonName
- smithy.api#length
- smithy.api#pattern
- smithy.api#range
- smithy.api#required
- smithy.api#timestampFormat
- alloy#uuidFormat
- alloy#discriminated
- alloy#untagged

#### Protocol Behavior and Semantics

##### Required Traits

All operations referenced by a `alloy#simpleRestJson` service must be annotated with the [http](https://awslabs.github.io/smithy/2.0/spec/http-bindings.html#http-trait) trait.

Errors referenced by any operation that's itself referenced by a `@simpleRestJson` service should be uniquely annotated by a status code (within the context of that operation), using the `@httpError` trait. It means that two errors referenced by a same operation cannot have the same `statusCode`. If several error shapes can be raised using a single `statusCode`, a `union` should be used to represent the alternatives.

##### Content-types

The `alloy#simpleRestJson` protocol uses a Content-Type of `application/json` : it should be set on http request/responses whenever an http body is present.

##### JSON Shape Serialization

| Smithy type | traits                                     | Json format                                                                                                                                                                                                                                                                                                                                                                                                                                     | Example                                  |
| ----------- | ------------------------------------------ | ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | ---------------------------------------- |
| blob        |                                            | Json string value, base64 encoded                                                                                                                                                                                                                                                                                                                                                                                                               | `ImhlbGxvIg==`                           |
| boolean     |                                            | Json boolean                                                                                                                                                                                                                                                                                                                                                                                                                                    | true                                     |
| byte        |                                            | Json number                                                                                                                                                                                                                                                                                                                                                                                                                                     | 1                                        |
| short       |                                            | Json number                                                                                                                                                                                                                                                                                                                                                                                                                                     | 1                                        |
| integer     |                                            | Json number                                                                                                                                                                                                                                                                                                                                                                                                                                     | 1                                        |
| long        |                                            | Json number                                                                                                                                                                                                                                                                                                                                                                                                                                     | 1                                        |
| float       |                                            | Json number                                                                                                                                                                                                                                                                                                                                                                                                                                     | 1.1                                      |
| double      |                                            | Json number                                                                                                                                                                                                                                                                                                                                                                                                                                     | 1.1                                      |
| bigDecimal  |                                            | Json number                                                                                                                                                                                                                                                                                                                                                                                                                                     | 111111                                   |
| bigInteger  |                                            | Json number                                                                                                                                                                                                                                                                                                                                                                                                                                     | 111111                                   |
| string      |                                            | Json string                                                                                                                                                                                                                                                                                                                                                                                                                                     | "hello"                                  |
| timestamp   | (none, or `@timestampFormat("date-time")`) | Json string, following the date-time section of [RFC3339, section 5.6](https://xml2rfc.tools.ietf.org/public/rfc/html/rfc3339.html#anchor14)                                                                                                                                                                                                                                                                                                    | 1985-04-12T23:20:50.52Z                  |
| timestamp   | `@timestampFormat("http-date")`            | Json string, following the `IMF-fixdate` section of [RFC 7231](https://datatracker.ietf.org/doc/html/rfc7231.html#section-7.1.1.1)                                                                                                                                                                                                                                                                                                              | Sun, 02 Jan 2000 20:34:56.000 GMT        |
| timestamp   | `@timestamp`                               | Json number, following Unix-time semantics, with optional fractional precision                                                                                                                                                                                                                                                                                                                                                                  | 1515531081.1234                          |
| document    |                                            | Json value (arbitrary shape)                                                                                                                                                                                                                                                                                                                                                                                                                    | [{"a": "b"}]                             |
| list        |                                            | Json array                                                                                                                                                                                                                                                                                                                                                                                                                                      | [1,2,2,3]                                |
| set         |                                            | Json array (with unique values)                                                                                                                                                                                                                                                                                                                                                                                                                 | [1, 2, 3]                                |
| map         |                                            | Json object                                                                                                                                                                                                                                                                                                                                                                                                                                     | {"a" : 1, "b" : 2}                       |
| structure   |                                            | Json object. Each member of the structure translates to a Json property when the name of the property is the same as the member name, unless that member is annotated with the [jsonName](https://awslabs.github.io/smithy/2.0/spec/protocol-traits.html#jsonname-trait). Members that are not annotated with the `required` trait can be omitted, or set to `null` to indicate an absence of value. Otherwise, the property values must be set | {"int": 1, "str": "hello"}               |
| union       |                                            | Same as structures, except that only a single member can be set to a non-null value.                                                                                                                                                                                                                                                                                                                                                            | {"foo": {"int": 1, "str": "hello" }      |
| union       | @discriminated("type")                     | Same as unions, except a discriminator field is included. This field specifies which branch of the union is included in the encoded JSON. All member shapes in a discriminated union must be structures.                                                                                                                                                                                                                                        | {"type": "foo","int": 1, "str": "hello"} |
| union       | @untagged                                  | Same as structure, but the encode/decoding logic does not know what to deserialize to. `untagged` is not recommended. It is the least efficient approach, it's available to support existing APIs.                                                                                                                                                                                                                                              | {"int": 1, "str": "hello"}               |

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

This protocol is aware of the following constructs and traits provided out of the box by the smithy language and its `smithy.api` standard library.

* [all simple shapes](https://awslabs.github.io/smithy/1.0/spec/core/model.html#simple-shapes)
* composite data shapes, including collections, unions, structures.
* [operations and services](https://awslabs.github.io/smithy/1.0/spec/core/model.html#service)
* [enumerations](https://awslabs.github.io/smithy/1.0/spec/core/constraint-traits.html#enum-trait)
* [error trait](https://awslabs.github.io/smithy/1.0/spec/core/type-refinement-traits.html#error-trait)
* [http traits](https://awslabs.github.io/smithy/1.0/spec/core/http-traits.html), including **http**, **httpError**, **httpLabel**, **httpHeader**, **httpPayload**, **httpQuery**, **httpPrefixHeaders**, **httpQueryParams**, **httpResponseCode**.
* [timestampFormat trait](https://awslabs.github.io/smithy/1.0/spec/core/protocol-traits.html?highlight=timestampformat#timestampformat-trait)

Furthermore, implementors of the protocol have to take into consideration additional traits that are defined by the `alloy` library :

- `alloy#untagged`
- `alloy#discriminated`
- `alloy#uuidFormat`
