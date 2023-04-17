$version: "2"

namespace alloy

/// A trait for specifying what example data looks like. Differs from the `smithy.api#examples` trait in that
/// it can be used for any shape, not just operations. Below is an explanation of the different example formats
/// that are supported.
/// 1. SMITHY - this means that the examples will be using the `Document` abstraction and will be specified in
/// a protocol agnostic way
/// 2. JSON - this means the examples will use the `Document` abstraction, but will not be validated by the smithy
/// `NodeValidationVisitor` like the first type are. This type can be used to specify protocol specific examples
/// 3. STRING - this is just a string example and anything can be provided inside of the string.
/// This can be helpful for showing e.g. xml or another encoding that isn't JSON and therefore doesn't fit nicely
///  with `Node` semantics
@trait(selector: ":not(:test(service, operation, resource))")
list dataExamples {
  member: DataExample
}

union DataExample {
  smithy: Document
  json: Document
  string: String
}
