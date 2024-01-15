### Protobuf serialisation

When protocols use protobuf as a serialisation, alloy also proposes a set of semantics for how smithy shapes translate to protobuf-specific concepts. In this document, we describe these semantics by explaining how the smithy code should translate to proto code representing the equivalent data.

`alloy` defines a number of traits that are aimed at capturing various semantics relative to [https://protobuf.dev/overview/](protobuf) serialisation that are not part of the smithy standard library.

For full documentation on what each of these traits does, see the smithy specification [here](modules/core/resources/META-INF/smithy/proto/proto.smithy).

#### Primitives

Below is a table describing how smithy shapes translate to proto constructs.

Protobuf supports a number of [scalar types](https://developers.google.com/protocol-buffers/docs/proto3#scalar) that do not have first class support in smithy. In order to allow for expressing some of these in smithy, `alloy` provides a `alloy.proto#protoNumType` trait that can refine the meaning of `Integer` or `Long` types in protobuf semantics.

Additionally, in the context of `alloy`, the absence of the `@required` trait is interpreted by referencing wrapper types present in the `google.protobuf` library, which permits the distinction between the absence of a value and the presence of a default value.

| Smithy type          | @protoNumType | @required | Proto                        |
| -------------------- | ------------- | --------- | ---------------------------- |
| bigDecimal           | N/A           | N/A       | message { string value = 1 } |
| bigInteger           | N/A           | N/A       | message { string value = 1 } |
| blob                 | N/A           | false     | google.protobuf.BytesValue   |
| blob                 | N/A           | true      | bytes                        |
| boolean              | N/A           | false     | google.protobuf.BoolValue    |
| boolean              | N/A           | true      | bool                         |
| double               | N/A           | false     | google.protobuf.DoubleValue  |
| double               | N/A           | true      | double                       |
| float                | N/A           | false     | google.protobuf.FloatValue   |
| float                | N/A           | true      | float                        |
| integer, byte, short | FIXED         | false     | google.protobuf.Int32Value   |
| integer, byte, short | FIXED         | true      | fixed32                      |
| integer, byte, short | FIXED_SIGNED  | false     | google.protobuf.Int32Value   |
| integer, byte, short | FIXED_SIGNED  | true      | sfixed32                     |
| integer, byte, short | N/A           | true      | google.protobuf.Int32Value   |
| integer, byte, short | N/A           | true      | int32                        |
| integer, byte, short | SIGNED        | false     | google.protobuf.Int32Value   |
| integer, byte, short | SIGNED        | true      | sint32                       |
| integer, byte, short | UNSIGNED      | false     | google.protobuf.UInt32Value  |
| integer, byte, short | UNSIGNED      | true      | uint32                       |
| long                 | FIXED         | false     | google.protobuf.Int64Value   |
| long                 | FIXED         | true      | fixed64                      |
| long                 | FIXED_SIGNED  | false     | google.protobuf.Int64Value   |
| long                 | FIXED_SIGNED  | true      | sfixed64                     |
| long                 | N/A           | true      | google.protobuf.Int64Value   |
| long                 | N/A           | true      | int64                        |
| long                 | SIGNED        | false     | google.protobuf.Int64Value   |
| long                 | SIGNED        | true      | sint64                       |
| long                 | UNSIGNED      | false     | google.protobuf.UInt64Value  |
| long                 | UNSIGNED      | true      | uint64                       |
| string               | N/A           | false     | google.protobuf.StringValue  |
| string               | N/A           | true      | string                       |
| timestamp            | N/A           | N/A       | message { long value = 1 }   |

_Note: the `@protoNumType` has no effect on non-required integer/long (except `UNSIGNED`). This is because there are no FIXED, FIXED_SIGNED or SIGNED instances in Google's protobuf wrappers_

#### UUIDs

`alloy` gives special meaning to the `alloy#UUID` shape in the context of protobuf : `UUIDs` are supposed to be encoding by means of a message containing two long values, which is more optimal than serialising a string.

Smithy:

```smithy
structure Foo {
  @required
  uuid : alloy#UUID
}

```

Proto:
```proto
message UUID {
  int64 upper_bits = 1;
  int64 lower_bits = 2;
}

message Foo {
  uuid: UUID
}
```

#### Aggregate Types

##### Structure

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

##### Union

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
  @required
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

##### List

Smithy:
```smithy
list StringArrayType {
    member: String
}
structure StringArray {
    value: StringArrayType
}
```

Proto:
```proto
message StringArray {
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

##### String Enum (closed)

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

##### String Enum (open)

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

##### Int Enum (closed)

NB : the value of int enums at the smithy level is irrelevant : unless otherwise specified, each enum value is allocated to a protobuf index.

Smithy:
```smithy
intEnum Color {
    RED = 6
    GREEN = 7
    BLUE = 8
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

##### Int Enum (open)

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

Marks an explicit index to be used for a member when it gets serialised to protobuf. For example:

```smithy
structure Test {
  str: String
}
```

Is equivalent to:

```proto
message Test {
  string str = 1;
}
```

Where the following:

```smithy
structure Test {
  @protoIndex(2)
  str: String
}
```

Is equivalent to:

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

Members of closed enumerations (whether string or int) can be annotated by `alloy.proto#protoIndex` in smithy to customise the corresponding proto index that should be used
during serialisation. An additional constraint is that when users elect to specify `alloy.proto#protoIndex`, they are required to assign the `0` value to one of the enumeration
members, as it is a requirement on the protobuf side.

On the other hand, members of open enumerations MUST NOT be annotated with `alloy.proto#protoIndex`, as open enumerations in Smithy translate to the raw string/int in protobuf,
allowing for the capture of unknown value regardless of how the target language generates enumerations.

#### alloy.proto#protoInlinedOneOf

This annotation can be used to customize the rendering on Unions in protobuf. When you add this annotation to a Union, you must make sure that this Union is used exactly once as part of a structure. A validator bundled in this library will ensure this is the case.

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

#### alloy.proto#protoNumType

Specifies the type of signing that should be used for integers and longs. Options are:

- SIGNED
- UNSIGNED
- FIXED
- FIXED_SIGNED

### Additional protobuf-related traits

#### alloy.proto#protoEnabled

This trait can be used by tooling to filter-in the list of shapes that should be taken into consideration when performing some protobuf-related validation or processing.
This is used, for example, by the [smithy-translate](https://github.com/disneystreaming/smithy-translate/) tool.

#### alloy.proto#protoReservedFields

This can be used by tooling to mark some fields as reserved, which can be helpful to prevent some backward/forward compatibility problems when using smithy to describe
protobuf/gRPC interactions.

It allows to mark certain field indexes as unusable by the smithy specification. For example, if a range is provided of 1 to 10 then the proto indexes
for any fields in that structure must fall outside of that range. Ranges are inclusive.
