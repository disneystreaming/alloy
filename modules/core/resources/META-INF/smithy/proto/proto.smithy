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
        protoEnabled
        uncheckedExamples
    ]
)
@trait(selector: "service")
structure grpc {}

/// Marks an explicit index to be used for a structure member when it is
/// interpreted as protobuf. For example:
/// structure Test {
/// str: String
/// }
/// Is equivalent to:
/// message Test {
/// string str = 1;
/// }
/// Where the following:
/// structure Test {
/// @protoIndex(2)
/// str: String
/// }
/// Is equivalent to:
/// message Test {
/// string str = 2;
/// }
@trait(selector: ":is(structure > member,union > member,enum > member)")
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
@trait(selector: ":test(simpleType, member > simpleType)")
structure protoWrapped {}

// indicates that string abiding by @alloy#uuidFormat should
// be encoded using a proto message containing 2 long values.
@trait(selector: ":test(string [trait|alloy#uuidFormat], member > string [trait|alloy#uuidFormat])")
structure protoCompactUUID {}
