$version: "2.0"

namespace alloy.test

use alloy#simpleRestJson
use smithy.test#httpRequestTests
use smithy.test#httpResponseTests

apply SparseQueryParam @httpRequestTests([
    {
        id: "SparseQueryParam"
        protocol: simpleRestJson
        method: "GET"
        uri: "/sparse-query-param"
        params: {
            foo: ["bar", null, "baz"]
        }
        queryParams: [
            "foo=bar",
            "foo",
            "foo=baz"
        ]

    }
])