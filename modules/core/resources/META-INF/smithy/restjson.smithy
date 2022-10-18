$version: "2"

namespace alloy

/// A rest protocol that primarily deals with JSON
/// in HTTP requests and responses. These are encoded with
/// the content type `application/json`. The exception is
/// for requests/responses of type Blob or String. These are
/// encoded as `application/octet-stream` and `text/plain`
/// respectively. See Alloy documentation for more information.
@protocolDefinition(traits: [
    smithy.api#error,
    smithy.api#required,
    smithy.api#pattern,
    smithy.api#range,
    smithy.api#length,
    smithy.api#http,
    smithy.api#httpError,
    smithy.api#httpHeader,
    smithy.api#httpLabel,
    smithy.api#httpPayload,
    smithy.api#httpPrefixHeaders,
    smithy.api#httpQuery,
    smithy.api#httpQueryParams,
    smithy.api#jsonName,
    smithy.api#timestampFormat,
    uncheckedExamples,
    uuidFormat,
    discriminated,
    untagged,
])
@trait(selector: "service")
structure simpleRestJson {}
