$version: "2"

namespace alloy.proto

use alloy#uncheckedExamples
use alloy#uuidFormat
/// GRPC protocol as defined by https://grpc.io/

@protocolDefinition(
    traits: [

        protoReservedFields
        protoIndex
        protoNumType
        protoTimestampFormat
        protoEnumFormat
        protoEnabled
        uncheckedExamples
    ]
)
@trait(selector: "service")
structure grpc {}

/// Marks an explicit index to be used for a structure member when it is
/// interpreted as protobuf. For example:
///
/// structure Test { @protoIndex(2) str: String }
///
/// Is equivalent to:
///
/// message Test { string str = 2 }
@trait(
    selector: ":is(structure > member,union > member,enum > member, intEnum > member)"
)
integer protoIndex

/// Specifies which type of number signing should be used on integers
/// and longs.
@trait(selector: ":test(integer, long, member > :test(integer, long))")
enum protoNumType {
    SIGNED
    UNSIGNED
    FIXED
    FIXED_SIGNED
}

/// Specifies which type of timestamp representation should be used
/// PROTOBUF indicates that the default encoding should be used
/// EPOCH_MILLIS indicates that an int64 should be used instead of the
/// default encoding.
/// RFC3339_STRING indicates that the timestamp should be encoded as a string
/// using the format described in RFC3339 section 5.6.
@trait(selector: ":test(timestamp, member > timestamp)")
enum protoTimestampFormat {
  PROTOBUF
  EPOCH_MILLIS
  RFC3339_STRING
}

/// Specifies how enumeration value should be encodeds in protobuf.
/// ORDINAL indicates that the enum value should be encoded as an integer.
/// STRING_VALUE indicates that the enum value should be encoded as the
/// string value.
@trait(selector: ":test(enum, member > enum)")
enum protoEnumFormat {
  ORDINAL
  STRING_VALUE
}

/// Marks certain field indexes as unusable by the smithy
/// specification. For example, if a range is provided of
/// 1 to 10 then the proto indexes for any fields in that
/// structure must fall outside of that range. Ranges
/// are inclusive.
@trait(selector: "structure")
list protoReservedFields {
    member: ReservedFieldsDefinition
}

union ReservedFieldsDefinition {
    name: String
    number: Integer
    range: Range
}

structure Range {
    @required
    start: Integer
    @required
    end: Integer
}

/// This trait can be used to enable protobuf conversion
/// on services or structures that are not a part of a
/// GRPC service.
@trait(selector: ":test(structure, service)")
structure protoEnabled {}

/// This trait can be used to customize the rendering of an
/// Union shape during the conversion to Protobuf models.
/// Union in Protobuf are typically encoded using `oneOf`.
///
/// `oneOf` can only be used within `message` and each of their
/// member has an index. This makes them tricky to render.
/// One possible solution is to create a synthetic `message` to
/// host the `oneOf`, and then use that `message` FQN at use site
/// when refering to the Union.
/// There is an alternate encoding where you render the `oneOf`
/// inside the `message` where it's used. You can only use this encoding
/// if the Union is used only inside of one `structure`.
///
/// You can use this trait, along with the validator provided, to
/// implement this encoding.
@trait(selector: "union")
structure protoInlinedOneOf {}

// This trait can be used to enforce values being wrapped in
// single-field messages, which allows for distinguishing between
// absence of values and default values.
@trait(
    selector: ":test(simpleType, list, map, member > simpleType, member > list, member > map)"
)
structure protoWrapped {}

// indicates that string abiding by @alloy#uuidFormat should
// be encoded using a proto message containing 2 long values.
@trait(
    selector: ":test(string [trait|alloy#uuidFormat], member > string [trait|alloy#uuidFormat])"
)
structure protoCompactUUID {}
