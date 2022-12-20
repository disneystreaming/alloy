$version: "2"

namespace alloy

@trait(
  selector: "structure > member :test(> :is(simpleType, list, map))",
  conflicts: [required]
)
document defaultValue

@trait()
structure nullable {}
