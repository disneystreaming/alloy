$version: "2"

namespace alloy

@trait(selector: "string")
structure structurePattern {
  @required
  pattern: String,
  @required
  @idRef(selector: "structure")
  target: String
}
