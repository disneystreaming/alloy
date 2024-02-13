$version: "2"

namespace alloy

/// Retain unknown fields of a containing structure in a map.
@trait(
    selector: "structure > member :test(> map :test(> member > document))"
)
structure unknownFieldRetention {}
