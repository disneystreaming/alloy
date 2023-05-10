$version: "2"

namespace alloy

/// A version of @examples that is not tied to a validator
@trait(selector: "operation")
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
