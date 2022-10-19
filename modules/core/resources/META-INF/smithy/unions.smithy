$version: "2"

namespace alloy

/// Discriminated unions contain the information about which
/// branch of a union is encoded inside of the object itself.
/// The following union:
/// structure One {
///   a: Int
/// }
/// structure Two {
///   b: String
/// }
/// union Test {
///   one: One
///   two: Two  
/// }
/// would normally be encoded in JSON as:
/// { "one": { "a": 123 } }
/// when tagged with `@discriminated("type")`, it will
/// instead be encoded as:
/// { "a": 123, "type": "one" }
/// This is more efficient than using an untagged encoding,
/// but less efficient than using the default tagged union
/// encoding. Therefore, it should only be used when necessary.
/// Tagged union encodings should be used wherever possible.
@trait(selector: "union :not([trait|alloy#untagged])")
string discriminated

/// Implies a different encoding for unions where
/// different alternatives are not tagged. This union type
/// should be avoided whenever possible for performance
/// reasons. However, some third party APIs use it so it
/// is important to be able to represent it.
/// The following union:
/// structure One {
///   a: Int
/// }
/// structure Two {
///   b: String
/// }
/// union Test {
///   one: One
///   two: Two  
/// }
/// would normally be encoded in JSON as
/// { "one": { "a": 123 } }
/// When it is annotated with `@untagged`, it is
/// instead encoded as:
/// { "a": 123 }. Therefore the parser will need to try
/// each different alternative in the union before it can
/// determine which one is appropriate.
@trait(selector: "union :not([trait|alloy#discriminated])")
structure untagged {}
