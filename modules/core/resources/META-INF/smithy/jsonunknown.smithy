$version: "2"

namespace alloy

/// Retain unknown fields of a containing structure in this map member
@trait(
    // selector explanation:
    // struct members that target a map with a `value` member targetting a document, and
    // union members that target a document
    selector: "
        :is(
            structure > member :test(> map > member[id|member=value] > document),
            union > member :test(> document)
        )
    "
    structurallyExclusive: "member"
)
structure jsonUnknown {}
