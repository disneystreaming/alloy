$version: "2"

namespace alloy

use alloy#GetIntEnum
use alloy#simpleRestJson
use smithy.test#httpRequestTests
use smithy.test#httpResponseTests




apply GetIntEnum @httpRequestTests([
    {
        id: "GetIntEnumInput"
        documentation: ""
        protocol: simpleRestJson
        uri : "/get-int-enum/1"
        method: "GET"
        params: {
            aa: 1
        }
    }
])
apply GetIntEnum @httpResponseTests([
    {
        id: "GetIntEnumOutput"
        documentation: ""
        protocol: simpleRestJson
        code: 200
        body: """
        {"result":"1"}
        """
        params: {
            result: 1
        }
    }
])