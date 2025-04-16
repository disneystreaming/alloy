$version: "2"

namespace alloy.test

use alloy#simpleRestJson
use smithy.test#httpRequestTests
use smithy.test#httpResponseTests

apply OpenUnions @httpRequestTests([
    {
        id: "OpenUnionsKnownTaggedUnionCase"
        documentation: "Pass a known tagged union value in an open union"
        protocol: simpleRestJson
        method: "PUT"
        uri: "/openUnions"
        body: """
            {"tagged": {"str": "string value"}}"""
        headers: { "Content-Type": "application/json" }
        requireHeaders: ["Content-Length"]
        params: {
            data: {
                tagged: { str: "string value" }
            }
        }
        bodyMediaType: "application/json"
    }
    {
        id: "OpenUnionsUnknownTaggedUnionCase"
        documentation: "Pass an unknown tagged union value in an open union"
        protocol: simpleRestJson
        method: "PUT"
        uri: "/openUnions"
        body: """
            {"tagged": {"whatisthis": {"nested": "something different"}}}"""
        headers: { "Content-Type": "application/json" }
        requireHeaders: ["Content-Length"]
        params: {
            data: {
                tagged: {
                    other: {
                        whatisthis: { nested: "something different" }
                    }
                }
            }
        }
        bodyMediaType: "application/json"
    }
    {
        id: "OpenUnionsKnownDiscriminatedUnionCase"
        documentation: "Pass a known discriminated union value in an open union"
        protocol: simpleRestJson
        method: "PUT"
        uri: "/openUnions"
        body: """
            {"discriminated": {"key": "smol", "content": "some string"}}"""
        headers: { "Content-Type": "application/json" }
        requireHeaders: ["Content-Length"]
        params: {
            data: {
                discriminated: {
                    smol: { content: "some string" }
                }
            }
        }
        bodyMediaType: "application/json"
    }
    {
        id: "OpenUnionsUnknownDiscriminatedUnionCase"
        documentation: "Pass an unknown discriminated union value in an open union"
        protocol: simpleRestJson
        method: "PUT"
        uri: "/openUnions"
        body: """
            {\"discriminated\": {\"key\": \"mysterious_and_important\", \"extras\": 42}}"""
        headers: { "Content-Type": "application/json" }
        requireHeaders: ["Content-Length"]
        params: {
            data: {
                discriminated: {
                    other: {
                        key: "mysterious_and_important"
                        extras: 42
                    }
                }
            }
        }
        bodyMediaType: "application/json"
    }
])

apply OpenUnions @httpResponseTests([
    {
        id: "OpenUnionsKnownTaggedUnionCase"
        code: 200
        documentation: "Return a known tagged union value in an open union"
        protocol: simpleRestJson
        body: """
            {"tagged": {"str": "string value"}}"""
        headers: { "Content-Type": "application/json" }
        params: {
            data: {
                tagged: { str: "string value" }
            }
        }
        bodyMediaType: "application/json"
    }
    {
        id: "OpenUnionsUnknownTaggedUnionCase"
        code: 200
        documentation: "Return an unknown tagged union value in an open union"
        protocol: simpleRestJson
        body: """
            {"tagged": {"whatisthis": {"nested": "something different"}}}"""
        headers: { "Content-Type": "application/json" }
        params: {
            data: {
                tagged: {
                    other: {
                        whatisthis: { nested: "something different" }
                    }
                }
            }
        }
        bodyMediaType: "application/json"
    }
    {
        id: "OpenUnionsKnownDiscriminatedUnionCase"
        code: 200
        documentation: "Return a known discriminated union value in an open union"
        protocol: simpleRestJson
        body: """
            {"discriminated": {"key": "smol", "content": "some string"}}"""
        headers: { "Content-Type": "application/json" }
        params: {
            data: {
                discriminated: {
                    smol: { content: "some string" }
                }
            }
        }
        bodyMediaType: "application/json"
    }
    {
        id: "OpenUnionsUnknownDiscriminatedUnionCase"
        code: 200
        documentation: "Return an unknown discriminated union value in an open union"
        protocol: simpleRestJson
        body: """
            {\"discriminated\": {\"key\": \"mysterious_and_important\", \"extras\": 42}}"""
        headers: { "Content-Type": "application/json" }
        params: {
            data: {
                discriminated: {
                    other: {
                        key: "mysterious_and_important"
                        extras: 42
                    }
                }
            }
        }
        bodyMediaType: "application/json"
    }
])
