## Protobuf serialisation

When protocols use protobuf as a serialisation, alloy also proposes a set of semantics for how smithy shapes translate to protobuf-specific concepts. In this document, we describe these semantics by explaining how the smithy code should translate to proto code representing the equivalent data.

`alloy` defines a number of traits that are aimed at capturing various semantics relative to [https://protobuf.dev/overview/](protobuf) serialisation that are not part of the smithy standard library.

For full documentation on what each of these traits does, see the smithy specification [here](modules/core/resources/META-INF/smithy/proto/proto.smithy).

Note that for convenience, `alloy` provides a module containing protobuf definitions that used downstream to ensure that the semantics described in this document are respected.

```
com.disneystreaming.alloy:alloy-protocol:x.y.z
```

### Validation

`alloy` comes with validators that verify the abidance of shapes to the rules described below. Note that these validators are protocol-specific, and
are only verifying shapes that belong to the transitive closure of shapes annotated with either `alloy.proto#grpc` or `alloy.proto#protoEnabled`.

### Primitives

Below is a table describing how smithy shapes translate to proto constructs.

Protobuf supports a number of [scalar types](https://developers.google.com/protocol-buffers/docs/proto3#scalar) that do not have first class support in smithy. In order to allow for expressing some of these in smithy, `alloy` provides a `alloy.proto#protoNumType` trait that can refine the meaning of `Integer` or `Long` types in protobuf semantics.


| Smithy type          | @protoNumType | Proto                                         |
| -------------------- | ------------- | --------------------------------------------- |
| boolean              | N/A           | bool                                          |
| bigDecimal           | N/A           | string                                        |
| bigInteger           | N/A           | string                                        |
| blob                 | N/A           | bytes                                         |
| double               | N/A           | double                                        |
| float                | N/A           | float                                         |
| string               | N/A           | string                                        |
| integer, byte, short | N/A           | int32                                         |
| integer, byte, short | FIXED         | fixed32                                       |
| integer, byte, short | FIXED_SIGNED  | sfixed32                                      |
| integer, byte, short | SIGNED        | sint32                                        |
| integer, byte, short | UNSIGNED      | uint32                                        |
| long                 | N/A           | int64                                         |
| long                 | FIXED         | fixed64                                       |
| long                 | FIXED_SIGNED  | sfixed64                                      |
| long                 | SIGNED        | sint64                                        |
| long                 | UNSIGNED      | uint64                                        |
| timestamp            | N/A           | message { long seconds = 1; long nanos = 2; } |

#### alloy.proto#protoWrapped

Additionally, in the context of `alloy`, the presence of the `@protoWrapped` trait is interpreted as requiring the primitive to be wrapped in a one-field message.

For instance :

```smithy
@protoWrapped
string MyString
```

would be converted to

```proto
message MyString {
  string value = 1;
}
```

When converting .smithy IDL files to .proto IDL, types from the `google.protobuf` library and the `alloy.protobuf` library can be used.
Using `protoWrapped` is interesting as it permits the distinction between the absence of a value and the presence of a default value.

| Smithy type          | @protoNumType |                                 |
| -------------------- | ------------- | ------------------------------- |
| float                | N/A           | google.protobuf.FloatValue      |
| blob                 | N/A           | google.protobuf.BytesValue      |
| boolean              | N/A           | google.protobuf.BoolValue       |
| double               | N/A           | google.protobuf.DoubleValue     |
| bigDecimal           | N/A           | alloy.protobuf.BigDecimalValue  |
| bigInteger           | N/A           | alloy.protobuf.BigIntegerValue  |
| string               | N/A           | google.protobuf.StringValue     |
| integer, byte, short | N/A           | google.protobuf.Int32Value      |
| integer, byte, short | FIXED         | alloy.protobuf.FixedInt32Value  |
| integer, byte, short | FIXED_SIGNED  | alloy.protobuf.SFixedInt32Value |
| integer, byte, short | SIGNED        | alloy.protobuf.SInt32Value      |
| integer, byte, short | UNSIGNED      | google.protobuf.UInt32Value     |
| long                 | N/A           | google.protobuf.SInt64Value     |
| long                 | FIXED         | alloy.protobuf.Fixed64Value     |
| long                 | FIXED_SIGNED  | alloy.protobuf.SFixed64Value    |
| long                 | SIGNED        | alloy.protobuf.SInt64Value      |
| long                 | UNSIGNED      | google.protobuf.UInt64Value     |

#### alloy.proto#protoNumType

Integer and Long shapes can be annotated with the `@alloy.proto#protoNumType` in order to signal what encoding should be used during protobuf serialisation.

- SIGNED
- UNSIGNED
- FIXED
- FIXED_SIGNED

See [here](https://protobuf.dev/programming-guides/proto3/#scalar) for documentation about these encodings.


#### UUIDs

By default, string shapes annotated with `@uuidFormat` are serialised as protobuf strings. However, a `alloy.proto#protoCompactUUID` trait is provided, which signals that the serialised form should be a message containing two int64 values :

Smithy:

```smithy
use alloy#uuidFormat
use alloy.proto#protoCompactUUID

@protoCompactUUID
@uuidFormat
string MyUUID

structure Foo {
  uuid : alloy#UUID
}
```

Proto:

```proto
message MyUUID {
  int64 upper_bits = 1;
  int64 lower_bits = 2;
}

message Foo {
  uuid: MyUUID
}
```

### Documents

Documents should be serialised using a protobuf message equivalent to the [`google.protobuf.Value`](https://github.com/protocolbuffers/protobuf/blob/5ecfdd76ef25f069cd84fac0b0fb3b95e2d61a34/src/google/protobuf/struct.proto#L62) type, which is commonly used in the protobuf ecosystem to represent [JSON values](https://protobuf.dev/reference/protobuf/google.protobuf/#value).

### Timestamp

Timestamps should be serialised using a protobuf message equivalent to the [`google.protobuf.Timestamp`](https://github.com/protocolbuffers/protobuf/blob/5ecfdd76ef25f069cd84fac0b0fb3b95e2d61a34/src/google/protobuf/timestamp.proto#L133) type, which is commonly used in the protobuf ecosystem to represent [Timestamp values](https://protobuf.dev/reference/protobuf/google.protobuf/#timestamp).


### Aggregate Types

In the absence of explicit `@protoIndex` traits on their members, the following rules is applied for structures/unions/string enumerations:

* In the case of structure and union members, the members should be treated as having an implicit protobuf field value starting from 1 for the first member, and increasing monotonically (by 1) for each subsequent member.
* In the case of string enumerations, the members should be treated as having an implicit protobuf field value string from 0 for the first member, and increasing monotonically (by 1) for each subsequent member.

#### Structures

Smithy:
```smithy
structure Testing {
  myString: String,
  myInt: Integer
}
```

Proto:
```proto
import "google/protobuf/wrappers.proto";

message Testing {
  google.protobuf.StringValue myString = 1;
  google.protobuf.Int32Value myInt = 2;
}
```

#### Unions

Unions in Smithy are tricky to translate to Protobuf because of the nature of `oneOf` : unions are first-class citizens in Smithy, whereas `oneOf` can only exist relatively to messages in proto. Therefore, the default encoding for unions in protobuf is equivalent to the one of a proto `message` that contains a `definition` field which is the `oneOf`. For example:

Smithy:
```smithy
structure Union {
  @required
  value: TestUnion
}

union TestUnion {
    num: Integer,
    txt: String
}
```

Proto:
```proto
message Union {
  foo.TestUnion value = 1;
}

message TestUnion {
  oneof definition {
    int32 num = 1;
    string txt = 2;
  }
}
```

It is possible to use the `alloy@protoInlinedOneOf` to indicate that a union should be encoded as if the corresponding `oneOf` was directly inlined in a message. This is subject to additional constraints, are `oneOf` field indices are supposed to flattened into the containing `message`'s' field indices. However, this encoding is more compact.

For example:

Smithy:
```smithy

use alloy.proto#protoInlinedOneOf

structure Union {
  value: TestUnion
}

@protoInlinedOneOf
union TestUnion {
    num: Integer,
    txt: String
}
```

Proto:
```proto
syntax = "proto3";

package foo;

message Union {
  oneof value {
    int32 num = 1;
    string txt = 2;
  }
}
```

##### Union members targeting collections

Protobuf doesn't allow `oneof` members to have `repeated` or `map` fields. As a result, a smithy union with a members targeting a collection shapes MUST
either have the `@protoWrapped` trait or target a collection shape have the `@protoWrapped` trait.

##### Inlined unions (`alloy.proto#protoInlinedOneOf`)

The `alloy.proto#protoInlinedOneOf` trait can be used to inline the corresponding `oneof` in a protobuf message. A union with this trait MUST be used exactly once, by a structure member.

For example, this is valid:

```smithy
structure Test {
  myUnion: MyUnion
}

@protoInlinedOneOf
union MyUnion {
  a: String,
  b: Integer
}
```

But this is not because the `MyUnion` is used in multiple shapes.

```smithy
structure Test {
  myUnion: MyUnion
}
structure OtherStruct {
  aUnion: MyUnion
}

@protoInlinedOneOf
union MyUnion {
  a: String,
  b: Integer
}
```

This is also invalid because `MyUnion` is never used.

```smithy
@protoInlinedOneOf
union MyUnion {
  a: String,
  b: Integer
}
```

##### List

Smithy:
```smithy
list StringList {
    member: String
}
structure Struct {
    value: StringList
}
```

Proto:
```proto
message Struct {
  repeated string value = 1;
}
```

##### Map

Smithy:
```smithy
map StringStringMapType {
    key: String,
    value: String
}
structure StringStringMap {
  value: StringStringMapType
}
```

Proto:
```proto
message StringStringMap {
  map<string, string> value = 1;
}
```

##### String Enumerations (closed)

Smithy:
```smithy
enum Color {
    RED
    GREEN
    BLUE
}
```

Proto:
```proto
enum Color {
  RED = 0;
  GREEN = 1;
  BLUE = 2;
}
```

##### String Enumerations (open)

Open string enumerations are considered as raw strings when serialised to protobuf :

Smithy:
```smithy
@alloy#openEnum
enum Color {
    RED
    GREEN
    BLUE
}

structure Foo {
    color: Color
}
```

Proto:
```proto
message Foo {
  String color = 1;
}
```

##### Integer Enumerations (closed)

Each value translates to the proto index. Because of this, one of the values MUST be 0, as proto enforces each enumeration to have a value set to 0.

Smithy:
```smithy
intEnum Color {
    RED = 0
    GREEN = 5
    BLUE = 6
}
```

Proto:
```proto
enum Color {
  RED = 0;
  GREEN = 5;
  BLUE = 6;
}
```

##### Integer Enumerations (open)

Open int enumerations are considered as raw integers when serialised to protobuf :

Smithy:
```smithy
@alloy#openEnum
enum Color {
    RED = 6
    GREEN = 7
    BLUE = 8
}

structure Foo {
    color: Color
}
```

Proto:
```proto
message Foo {
  int32 color = 1;
}
```

#### alloy.proto#protoIndex

The `alloy.proto#protoIndex` trait marks an explicit index to be used for a member when it gets serialised to protobuf. For example:

the following

```smithy
structure Test {
  @protoIndex(2)
  str: String
}
```

has the following meaning in protobuf semantics

```proto
message Test {
  string str = 2;
}
```

When one member is annotated with a `@protoIndex`, all members have to be annotated with it. This includes the members of :

* structures
* unions
* (closed) enumerations

##### protoIndex for enumerations

Members of closed enumerations (whether string or int) can be annotated by `alloy.proto#protoIndex` in smithy to customise the corresponding proto index that should be used during serialisation. An additional constraint is that when users elect to specify `alloy.proto#protoIndex`, they are required to assign the `0` value to one of the enumeration members, as it is a requirement for protobuf.

On the other hand, members of open enumerations MUST NOT be annotated with `alloy.proto#protoIndex`, as open enumerations in Smithy translate to the raw string/int in protobuf, allowing for the capture of unknown value regardless of how the target language generates enumerations.

### Additional protobuf-related traits

#### alloy.proto#protoEnabled

This trait can be used by tooling to filter-in the list of shapes that should be taken into consideration when performing some protobuf-related validation or processing. This is used, for example, by the [smithy-translate](https://github.com/disneystreaming/smithy-translate/) tool.

#### alloy.proto#protoReservedFields

This can be used by tooling to mark some fields as reserved, which can be helpful to prevent some backward/forward compatibility problems when using smithy to describe protobuf/gRPC interactions.

It allows to mark certain field indexes as unusable by the smithy specification. For example, if a range is provided of 1 to 10 then the proto indexes
for any fields in that structure must fall outside of that range. Ranges are inclusive.
