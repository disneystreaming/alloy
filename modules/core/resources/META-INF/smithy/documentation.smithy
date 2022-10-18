$version: "2"

namespace alloy

@trait(selector: "operation")
/// A version of @examples that is not tied to a validator
list uncheckedExamples {
    member: UncheckedExample
}

@private
structure UncheckedExample {
    @required
    title: String
    documentation: String
    input: Document
    output: Document
}
