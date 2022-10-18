$version: "2"

namespace alloy.proto

use alloy#uncheckedExamples

/// GRPC protocol as defined by https://grpc.io/
@protocolDefinition(traits: [
    protoReservedFields,
    protoIndex,
    protoNumType,
    protoEnabled,
    uncheckedExamples
])
@trait(selector: "service")
structure grpc {}

/// Marks an explicit index to be used for a structure member when it is
/// interpreted as protobuf. For example:
/// structure Test {
///   str: String
/// }
/// Is equivalent to:
/// message Test {
///   string str = 1;
/// }
/// Where the following:
/// structure Test {
///   @protoIndex(2)
///   str: String
/// }
/// Is equivalent to:
/// message Test {
///   string str = 2;
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
