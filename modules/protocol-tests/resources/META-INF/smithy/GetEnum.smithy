$version: "2"

namespace alloy.test


use alloy.test#GetEnum
use alloy#simpleRestJson
use smithy.test#httpRequestTests
use smithy.test#httpResponseTests

apply GetEnum @httpRequestTests([
    {
        id: "GetEnumInput"
        protocol: simpleRestJson
        uri : "/get-enum/v1"
        method: "GET"
        params: {
            aa: "v1"
        }
    }
])
apply GetEnum @httpResponseTests([
    {
        id: "GetEnumOutput"
        protocol: simpleRestJson
        code: 200
        body: """
        {"result":"v1"}"""
        params: {
            result: "v1"
        }
    }
])


