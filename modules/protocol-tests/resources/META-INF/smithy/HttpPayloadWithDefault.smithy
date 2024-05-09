$version: "2"

namespace alloy.test

use alloy#simpleRestJson
use smithy.test#httpRequestTests
use smithy.test#httpResponseTests

apply HttpPayloadWithDefault @httpRequestTests([{
    id: "SimpleRestJsonSomeHttpPayloadWithDefault"
    documentation: "Pass JSON string value as is if payload provided"
    protocol: simpleRestJson
    method: "PUT"
    uri: "/httpPayloadWithDefault"
    body: "\"custom value\""
    headers: {
        "Content-Type": "application/json"
    }
    requireHeaders: ["Content-Length"]
    params: {
        body: "custom value"
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
        body: "default value"
    }
}])

apply HttpPayloadWithDefault @httpResponseTests([{
    id: "SimpleRestJsonSomeHttpPayloadWithDefault"
    documentation: "Pass simple string value as is if payload provided"
    protocol: simpleRestJson
    code: 200
    body: "\"custom value\""
    bodyMediaType: "application/json"
    headers: {
        "Content-Type": "application/json"
    }
    params: {
        body: "custom value"
    }
}, {
    id: "SimpleRestJsonNoneHttpPayloadWithDefault"
    documentation: "Use default value when there is no payload"
    protocol: simpleRestJson
    code: 200
    headers: {
        "Content-Type": "application/json"
    }
    params: {
        body: "default value"
    }
}])
