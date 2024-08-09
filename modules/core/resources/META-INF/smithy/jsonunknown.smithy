$version: "2"

namespace alloy

/// Retain unknown fields of a containing structure in this document member
@trait(
    selector: "structure > member :test(> map > member > document)"
    structurallyExclusive: "member"
)
structure jsonUnknown {}
