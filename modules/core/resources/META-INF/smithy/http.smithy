$version: "2"

namespace alloy

@trait(
    selector: "structure > member[trait|required] :test(> :test(union))",
    structurallyExclusive: "member"
)
structure httpPolymorphicResponse {}

@trait(selector: "structure")
integer httpSuccess
