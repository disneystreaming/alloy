$version: "2"

namespace alloy
use alloy#simpleRestJson
use alloy#Version
use smithy.test#httpResponseTests


apply Version @httpResponseTests([
    {
        id: "VersionOutput"
        protocol: simpleRestJson
        uri : "/version"
        method: "GET"
        body: """
        {"version":"1.0"}
        """
        params:{
            "version": "1.0"
        }
    }
])

