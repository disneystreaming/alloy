$version: "2"

namespace alloy

/// This trait indicates that a String value contains a date without
/// a time component.
/// e.g.: "2022-03-30"
/// If a time compenent is required, you can use smithy.api#Timestamp
@trait(selector: ":test(string, member > string)")
structure dateFormat { }