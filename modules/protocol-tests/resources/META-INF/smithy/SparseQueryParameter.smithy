$version: "2.0"

namespace alloy.test

use alloy#simpleRestJson
use smithy.test#httpRequestTests
use smithy.test#httpResponseTests

apply SparseQueryParam @httpRequestTests([
    {
        id: "SparseQueryParam"
        documentation: "Tests sparse query param serialization"
        protocol: simpleRestJson
        method: "GET"
        uri: "/sparseQueryParam"
        params: {
            foo: ["bar", null, "baz", ""]
        }
        queryParams: [
            "foo=bar",
            "foo",
            "foo=baz",
            "foo="
        ]

    }
])
