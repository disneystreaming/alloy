$version: "2"

namespace alloy

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
structure restJson {}
