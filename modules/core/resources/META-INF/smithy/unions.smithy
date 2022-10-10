$version: "2"

namespace alloy

@trait(selector: "union :not([trait|alloy#untagged])")
string discriminated

@trait(selector: "union :not([trait|alloy#discriminated])")
structure untagged {}
