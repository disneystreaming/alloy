$version: "2"

namespace alloy

/// Retain unknown fields of a containing structure in a map.
/// This variant controls the behaviour for document codecs.
@trait(
    selector: "structure > member :test(> document)"
)
structure unknownDocumentFieldRetention {}

/// Retain unknown fields of a containing structure in a map.
/// This variant controls the behaviour for JSON codecs.
@trait(
    selector: "structure > member :test(> document)"
)
structure unknownJsonFieldRetention {}
