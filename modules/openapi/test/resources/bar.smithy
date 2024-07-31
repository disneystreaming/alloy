$version: "2"

namespace bar

use alloy#simpleRestJson

@simpleRestJson
service BarService {
    operations: [BarOp]
}

@http(method: "GET", uri: "/bar")
operation BarOp {
    output := {
        out: CatOrDog
    }
}

union CatOrDog {
    one: String
    two: Integer
}

structure ProblemSomething {}

@alloy#discriminated("type")
union Problem {
    something: ProblemSomething
}
