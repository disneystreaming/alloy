$version: "2"

namespace alloy.test

use alloy#simpleRestJson
use alloy.test#Health
use smithy.test#httpRequestTests
use smithy.test#httpResponseTests


apply Health @httpRequestTests([
    {
        id: "HealthGet",
        documentation: "Checks that the /health endpoint returns a 200 response",
        protocol: simpleRestJson
        uri: "/health",
        method: "GET",
        body: "",
        queryParams: [
            "query=hello"
        ],
        params: {
            query: "hello"
        }
    }

])
