<!-- Using `yzhang.markdown-all-in-one` VS Code extension to create the table of contents -->
# Alloy <!-- omit in toc -->

A collection of commonly used Smithy shapes.

## Table of Contents <!-- omit in toc -->

- [Why Alloy?](#why-alloy)
- [Core alloy library](#core-alloy-library)
  - [Constraints and behavioural traits](#constraints-and-behavioural-traits)
  - [Serialisation](#serialisation)
  - [Protocols](#protocols)
- [Protocol Compliance Module](#protocol-compliance-module)
- [Working on Alloy](#working-on-alloy)
  - [Publish Local](#publish-local)
  - [Run Tests](#run-tests)


## Why Alloy?

Alloy is smithy library that contains traits and protocols that are not currently provided by the [smithy standard library](https://github.com/smithy-lang/smithy/blob/main/smithy-model/src/main/resources/software/amazon/smithy/model/loader/prelude.smithy). Alloy can be seen as a companion library to the smithy standard library (`smithy.api`).

The goals of alloy are :

* provide traits aiming at expressing protobuf/gRPC semantics in smithy
* provide traits allowing to capture patterns and constraints that are common in the industry (some related to http APIs, some more general)

## Core alloy library

The core alloy library, containing shapes and validators, is published to Maven Central at the following coordinates.

```
com.disneystreaming.alloy:alloy-core:x.y.z
```

It contains, in particular, traits and validators associated to the following aspects :

### Constraints and behavioural traits

Alloy provides a number of [constraint and behavioural](./modules/docs/misc/constraints.md) traits that may be leverage by tooling and protocols.

### Serialisation

Alloy defines a number of behavioural traits that can be leveraged by protocols to tweak serialisation. In particular for the following formats :

- [JSON](./modules/docs/serialisation/json.md)
- [Protobuf](./modules/docs/serialisation/protobuf.md)
- [URL Form Data](./modules/docs/serialisation/urlform.md)

### Protocols

Alloy defines two protocols :

- [`alloy#simpleRestJson`](./modules/docs/protocols/SimpleRestJson.md)
- [`alloy#grpc`](./modules/docs/protocols/gRPC.md)


## Protocol Compliance Module

Alloy provides a suite of protocol tests that utilise the [AWS HTTP Protocol Compliance Test Module]("https://smithy.io/2.0/additional-specs/http-protocol-compliance-tests.html. These tests accompany the specification of the `alloy#simpleRestJson` protocol, allowing implementations of that protocol to build confidence that the implemented behaviour is correct as per the specification.

These tests are available on maven central at the following coordinates :

```
com.disneystreaming.alloy:alloy-protocol-tests:x.y.z
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
