$version: "2"

namespace alloy

/// Specifies that an enumeration is open meaning that
/// it can accept "unknown" values that are not explicitly
/// specified inside of the smithy enum shape definition.
@trait(selector: ":test(enum, intEnum, [trait|enum])")
structure openEnum {}
