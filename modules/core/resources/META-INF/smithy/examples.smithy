$version: "2"

namespace alloy

@trait(selector: ":not(:test(service, operation, resource))")
list dataExamples {
  member: DataExample
}

union DataExample {
  smithy: Document
  json: Document
  string: String
}
