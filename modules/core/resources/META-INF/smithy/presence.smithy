$version: "2"

namespace alloy

/// Use this trait to give a default value to a structure member. This
/// is not the same as smithy.api#default which is more constrained.
/// You can use `defaultValue` to specify a default that does not align
/// with the target's shape constraints, where as Smithy's `default` trait
/// prevents that. For example:
///
/// ```smithy
/// @length(min:5)
/// string MyString
/// structure MyStruct {
///   @defaultValue("N/A") // that's valid
///   s1: MyString
///   s2: MyString = "N/A" // that's invalid
/// }
/// ```
@trait(
  selector: "structure > member :test(> :is(simpleType, list, map))",
  conflicts: [required]
)
document defaultValue


/// Use this trait to mark some field as nullable. This is to make
/// a distinction between an optional field that is missing and one
/// that's explicitly set to null.
@trait(selector: ":not([trait|trait])")
structure nullable {}
