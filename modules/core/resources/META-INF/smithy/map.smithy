$version: "2"

namespace alloy

/// This trait denotes that the order of keys in a map should be preserved
/// when being serialized and deserialized
@trait(
    selector: ":is(map)"
)
structure preserveKeyOrder {}
