$version: "2"

namespace alloy.openapi

/// This traits allows the encoding of OpenAPI Extensions
/// as defined in https://swagger.io/docs/specification/openapi-extensions/.
@trait
@sparse
map openapiExtensions {
    key: String
    value: Document
}

@trait(selector: "operation")
@length(min: 1)
string summary
