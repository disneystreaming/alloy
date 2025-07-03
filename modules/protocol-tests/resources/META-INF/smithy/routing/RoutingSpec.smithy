$version: "2"

namespace alloy.test.routing

use alloy#simpleRestJson
use smithy.test#httpRequestTests

apply Abc @httpRequestTests([
    {
        id: "RoutingAbc"
        documentation: "Test the Abc operation"
        protocol: simpleRestJson
        method: "GET"
        uri: "/abc"
        code: 200
        params: {}
    }
])

apply AbcDef @httpRequestTests([
    {
        id: "RoutingAbcDef"
        documentation: "Test the AbcDef operation"
        protocol: simpleRestJson
        method: "GET"
        uri: "/abc/def"
        code: 200
        params: {}
    }
])

apply AbcLabel @httpRequestTests([
    {
        id: "RoutingAbcLabel"
        documentation: "Test the AbcLabel operation with a label"
        protocol: simpleRestJson
        method: "GET"
        uri: "/abc/notDef"
        code: 200
        params: { def: "notDef" }
    }
])

apply AbcXyz @httpRequestTests([
    {
        id: "RoutingAbcXyz"
        documentation: "Test the AbcXyz operation"
        protocol: simpleRestJson
        method: "GET"
        uri: "/abc/xyz"
        code: 200
        params: {}
    }
])

apply AbcDefGreedy @httpRequestTests([
    {
        id: "RoutingAbcDefGreedy"
        documentation: "Test the AbcDefGreedy operation with a greedy label"
        protocol: simpleRestJson
        method: "GET"
        uri: "/abc/def/def"
        code: 200
        params: { def: "def/def" }
    }
])
