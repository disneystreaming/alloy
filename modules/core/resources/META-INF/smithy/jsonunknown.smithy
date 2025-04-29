$version: "2"

namespace alloy

/// Retain unknown fields of a containing structure in this member.
/// In case of unions, retain unknown cases in this member (open unions).
/// For both discriminated and tagged unions, this retains the entire object.
/// For untagged unions, having a Document member has the same effect.
@trait(
    // selector explanation:
    // struct members that target a map with a `value` member targetting a document, and
    // union members that target a document
    selector: "
        :is(
            structure > member :test(> map > member[id|member=value] > document),
            union:not([trait|alloy#untagged]) > member :test(> document)
        )
    "
    structurallyExclusive: "member"
    conflicts: [jsonName]
)
structure jsonUnknown {}
