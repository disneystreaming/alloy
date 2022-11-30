$version: "2"

namespace alloy.test

use alloy#simpleRestJson
use alloy.test#RoundTrip
use smithy.test#httpRequestTests
use smithy.test#httpResponseTests


apply RoundTrip @httpRequestTests([
    {
        id:"RoundTripRequest"
        protocol: simpleRestJson
        uri: "/roundTrip/thelabel"
        method: "POST"
        headers: {
            "HEADER": "the header"
        },
        queryParams:{
            "query": "the query"
        }
        body : "the body"
        params: {
            label: "thelabel",
            header: "the header",
            query: "the query",
            body: "the body"
        }
    }
])

apply RoundTrip @httpResponseTests([
    {
        id: "RoundTripDataResponse"
        protocol: simpleRestJson
        code: 200
        body : "the body"
        headers: {
            "HEADER": "the header"
        },
        params: {
            label: "thelabel",
            header: "the header",
            query: "the query",
            body: "the body"
        }
    }
])
