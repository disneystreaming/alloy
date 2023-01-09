$version: "2"

namespace alloy.test

use alloy#simpleRestJson
use alloy.test#HeaderEndpoint
use smithy.test#httpRequestTests
use smithy.test#httpResponseTests


apply HeaderEndpoint @httpRequestTests([
    {
        id: "HeaderEndpointInput",
        documentation: "tests variety of casing scenarios for writing http headers",
        protocol: simpleRestJson,
        method: "POST",
        uri: "/headers"
        headers: {
            "X-UPPERCASE-HEADER": "UPPERCASE_VALUE",
            "X-Capitalized-Header": "Capitalized_value",
            "x-lowercase-header": "lowercase_value"
            "x-MiXeD-hEaDEr": "aLLMiXedUP"
        }
        body: ""
        params: {
            uppercaseHeader: "UPPERCASE_VALUE",
            capitalizedHeader: "Capitalized_value",
            lowercaseHeader: "lowercase_value",
            mixedHeader: "aLLMiXedUP",
        }
    }
])
apply HeaderEndpoint @httpResponseTests([
    {
        id: "headerEndpointResponse"
        protocol: simpleRestJson
        documentation: "tests variety of casing scenarios for reading http headers"
        code: 200
        headers: {
            "X-UPPERCASE-HEADER": "UPPERCASE_VALUE",
            "X-Capitalized-Header": "Capitalized_value",
            "x-lowercase-header": "lowercase_value"
            "x-MiXeD-hEaDEr": "aLLMiXedUP"
        }
        params: {
            uppercaseHeader: "UPPERCASE_VALUE",
            capitalizedHeader: "Capitalized_value",
            lowercaseHeader: "lowercase_value",
            mixedHeader: "aLLMiXedUP",
        }
    }

])
