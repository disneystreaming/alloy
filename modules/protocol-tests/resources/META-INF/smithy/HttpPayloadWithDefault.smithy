$version: "2"

namespace alloy.test

use alloy#simpleRestJson
use smithy.test#httpRequestTests
use smithy.test#httpResponseTests

apply HttpPayloadWithDefault @httpRequestTests([{
    id: "SimpleRestJsonSomeHttpPayloadWithDefault"
    documentation: "Pass simple string value as is if payload provided"
    protocol: simpleRestJson
    method: "PUT"
    uri: "/httpPayloadWithDefault"
    body: "\"custom value\""
    headers: {
        "Content-Type": "application/json"
    }
    requireHeaders: ["Content-Length"]
    params: {
        nested: "custom value"
    }
    bodyMediaType: "application/json"
}, {
    id: "SimpleRestJsonNoneHttpPayloadWithDefault"
    documentation: "Use default value when there is no payload"
    protocol: simpleRestJson
    method: "PUT"
    uri: "/httpPayloadWithDefault"
    headers: {
        "Content-Type": "application/json"
    }
    params: {
        nested: "default value"
    }
}])
