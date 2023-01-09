$version: "2"

namespace alloy.test

use alloy#simpleRestJson
use alloy.test#Version
use smithy.test#httpResponseTests

apply Version @httpResponseTests([
    {
        id: "VersionOutput"
        protocol: simpleRestJson
        code: 200
        body: """
            "1.0" """
        params:{
            "version": "1.0"
        }
    }
])
