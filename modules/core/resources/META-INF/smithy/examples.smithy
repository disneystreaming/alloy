$version: "2"

namespace alloy

@trait(selector: ":not(:test(service, operation, resource))")
list dataExamples {
  member: Document
}