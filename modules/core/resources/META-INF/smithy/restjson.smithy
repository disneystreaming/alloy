$version: "2"

namespace alloy

/// A rest protocol that deals with JSON payloads only
/// in HTTP requests and responses. These are encoded with
/// the content type `application/json`.
/// See Alloy documentation for more information.
@protocolDefinition(traits: [

    smithy.api#default
    smithy.api#error
    smithy.api#http
    smithy.api#httpError
    smithy.api#httpHeader
    smithy.api#httpLabel
    smithy.api#httpPayload
    smithy.api#httpPrefixHeaders
    smithy.api#httpQuery
    smithy.api#httpQueryParams
    smithy.api#httpResponseCode
    smithy.api#jsonName
    smithy.api#length
    smithy.api#pattern
    smithy.api#range
    smithy.api#required
    smithy.api#timestampFormat
    uncheckedExamples
    uuidFormat
    discriminated
    untagged
])
@trait(selector: "service")
structure simpleRestJson {}
