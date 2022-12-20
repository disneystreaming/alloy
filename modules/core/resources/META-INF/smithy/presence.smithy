$version: "2"

namespace alloy

/// Use this trait to mark some field as nullable. This is to make
/// a distinction between an optional feel that's missing and one
/// that's explicitly set to null.
@trait()
structure nullable {}
