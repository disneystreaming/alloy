$version: "2"

namespace alloy.test.routing

use alloy#simpleRestJson

// Used for testing https://smithy.io/2.0/spec/http-bindings.html#specificity-routing
@simpleRestJson
service RoutingService {
    version: "1.0.0"
    operations: [
        Abc
        AbcDef
        AbcLabel
        AbcXyz
        AbcDefGreedy
    ]
}

@readonly
@http(method: "GET", uri: "/abc", code: 200)
operation Abc {
    output: MessageOutput
}

@readonly
@http(method: "GET", uri: "/abc/def", code: 200)
operation AbcDef {
    output: MessageOutput
}

@readonly
@http(method: "GET", uri: "/abc/{def}", code: 200)
operation AbcLabel {
    input := {
        @httpLabel
        @required
        def: String
    }

    output: MessageOutput
}

@readonly
@http(method: "GET", uri: "/abc/xyz", code: 200)
operation AbcXyz {
    output: MessageOutput
}

@readonly
@http(method: "GET", uri: "/abc/{def+}", code: 200)
operation AbcDefGreedy {
    input := {
        @httpLabel
        @required
        def: String
    }

    output: MessageOutput
}

structure MessageOutput {
    @httpPayload
    @required
    message: String
}
