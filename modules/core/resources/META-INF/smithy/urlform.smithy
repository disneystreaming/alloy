$version: "2"

namespace alloy

/// Unwraps the values of a list, set, or map into the containing
/// structure/union.
@trait(
    selector: ":is(structure, union) > :test(member > :test(list, map))"
    breakingChanges: [
        {change: "any"}
    ]
)
structure urlFormFlattened {}

/// Changes the serialized element or attribute name of a structure, union,
/// or member.
@trait(
    selector: ":is(structure, union, member)"
    breakingChanges: [
        {change: "any"}
    ]
)
@pattern("^[a-zA-Z_][a-zA-Z_0-9-]*$")
string urlFormName
