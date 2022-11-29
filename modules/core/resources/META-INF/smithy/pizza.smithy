$version: "2"

namespace smithy4s.example

use alloy#simpleRestJson

use smithy.test#httpRequestTests
use smithy.test#httpResponseTests

@simpleRestJson
service PizzaAdminService {
    version: "1.0.0",
    errors: [GenericServerError, GenericClientError],
    operations: [AddMenuItem, GetMenu, Version, Health, HeaderEndpoint, RoundTrip, GetEnum, GetIntEnum, CustomCode]
}

@httpRequestTests([
    {
        id: "AddMenuItem",
        protocol: simpleRestJson,
        method: "POST"
        uri: "/restaurant/{restaurant}/menu/item"
        body: """
        {
        "food":"pepper steak","price":20.0
        }
        """,
        params: {
          restaurant : "Uncle Mike's"
        }
        bodyMediaType: "application/json",
    }
])
@httpResponseTests([
    {
        id: "AddMenuItemResult",
        protocol: simpleRestJson,
        code: 201,
        params: {
            itemId: "1",
            added: 1576540098
        }
        body: """
        {"itemId":"1"}
        """
        bodyMediaType: "application/json",
        headers: { "X-ADDED-AT": "1576540098"}
    },
    {
        id: "PriceError",
        protocol: simpleRestJson,
        code: 400,
        params: {
            PriceError: {message:"invalid price",code: 400 }
        }
        body: """
        {"priceError" :{"message":"invalid price"}}
        """
        bodyMediaType: "application/json",
        headers: { "X-CODE": "400"}
    }
])
@http(method: "POST", uri: "/restaurant/{restaurant}/menu/item", code: 201)
operation AddMenuItem {
    input: AddMenuItemRequest,
    errors: [PriceError],
    output: AddMenuItemResult
}



@httpRequestTests([
    {
        id: "HeaderEndpointInput",
        protocol: simpleRestJson,
        method : "POST",
        uri: "/headers/"
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
@httpResponseTests([
    {
        id:"headerEndpointResponse"
        protocol: simpleRestJson
        code:200
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
@http(method: "POST", uri: "/headers/", code: 200)
operation HeaderEndpoint {
    input: HeaderEndpointData,
    output: HeaderEndpointData
}

@httpRequestTests([
    {
        id:"RoundTripRequest"
        protocol: simpleRestJson
        uri: "/roundTrip/{label}"
        method: "POST"
        headers: {
            "HEADER": "the header"
        },
        queryParams:{
            "query": "the query"
        }
        body : "the body"
        params: {
              label: "the label",
             header: "the header",
              query: "the query",
              body: "the body"
    }
}
])

@httpresponseTests([
    {
        id: "RoundTripDataResponse"
        protocol: simpleRestJson
        code: 200
        body : "the body"
        headers: {
            "HEADER": "the header"
        },
        params: {
            label: "the label",
            header: "the header",
            query: "the query",
            body: "the body"
        }
    }
])
@http(method: "POST", uri: "/roundTrip/{label}", code: 200)
operation RoundTrip {
    input: RoundTripData,
    output: RoundTripData
}

structure RoundTripData {
    @httpLabel
    @required
    label: String,
    @httpHeader("HEADER")
    header: String,
    @httpQuery("query")
    query: String,
    body: String
}

structure HeaderEndpointData {
    @httpHeader("X-UPPERCASE-HEADER")
    uppercaseHeader: String,
    @httpHeader("X-Capitalized-Header")
    capitalizedHeader: String,
    @httpHeader("x-lowercase-header")
    lowercaseHeader: String,
    @httpHeader("x-MiXeD-hEaDEr")
    mixedHeader: String,
}

structure AddMenuItemResult {
    @httpPayload
    @required
    itemId: String,
    @timestampFormat("epoch-seconds")
    @httpHeader("X-ADDED-AT")
    @required
    added: Timestamp
}



@httpResponseTests([
    {
        id: "VersionOutput"
        protocol: simpleRestJson
        uri : "/version"
        method: "GET"
        body: """
        {"version":"1.0"}
        """
        params:{
            "version": "1.0"
        }
    }
])

@readonly
@http(method: "GET", uri: "/version", code: 200)
operation Version {
    output: VersionOutput
}


structure VersionOutput {
    @httpPayload
    @required
    version: String
}

@error("client")
structure PriceError {
    @required
    message: String,
    @required
    @httpHeader("X-CODE")
    code: Integer
}

@http(method: "GET", uri: "/restaurant/{restaurant}/menu", code: 200)
operation GetMenu {
    input: GetMenuRequest,
    errors: [NotFoundError, FallbackError],
    output: GetMenuResult
}

structure GetMenuRequest {
    @httpLabel
    @required
    restaurant: String
}

structure GetMenuResult {
    @required
    @httpPayload
    menu: Menu
}

@error("client")
@httpError(404)
structure NotFoundError {
    @required
    name: String
}

@error("client")
structure FallbackError {
    @required
    error: String
}

map Menu {
    key: String,
    value: MenuItem
}

structure AddMenuItemRequest {
    @httpLabel
    @required
    restaurant: String,
    @httpPayload
    @required
    menuItem: MenuItem
}

structure MenuItem {
    @required
    food: Food,
    @required
    price: Float
}

union Food {
    pizza: Pizza,
    salad: Salad
}

structure Salad {
    @required
    name: String,
    @required
    ingredients: Ingredients
}

structure Pizza {
    @required
    name: String,
    @required
    base: PizzaBase,
    @required
    toppings: Ingredients
}

@enum([{name: "CREAM", value: "C"}, {name: "TOMATO", value: "T"}])
string PizzaBase

@enum([{value: "Mushroom"}, {value: "Cheese"}, {value: "Salad"}, {value: "Tomato"}])
string Ingredient

list Ingredients {
    member: Ingredient
}

@error("server")
@httpError(502)
structure GenericServerError {
    @required
    message: String
}

@error("client")
@httpError(418)
structure GenericClientError {
    @required
    message: String
}

@readonly
@http(method: "GET", uri: "/health", code: 200)
operation Health {
    input: HealthRequest,
    output: HealthResponse,
    errors: [ UnknownServerError ]
}

structure HealthRequest {
    @httpQuery("query")
    @length(min: 0, max: 5)
    query: String
}

@freeForm(i : 1, a: 2)
structure HealthResponse {
    @required
    status: String
}

// This error indicates a fatal, unexpected error has occurred. Fallback strategies ought to be triggered by this
// error.
@error("server")
@httpError(500)
structure UnknownServerError {
    @required
    errorCode: UnknownServerErrorCode,

    description: String,

    stateHash: String
}

// Define the singular error code that can be returned for an UnknownServerError
@enum([
    {
        value: "server.error",
        name: "ERROR_CODE"
    }
])
string UnknownServerErrorCode


@trait
document freeForm

@readonly
@http(method: "GET", uri: "/get-enum/{aa}", code: 200)
operation GetEnum {
    input: GetEnumInput,
    output: GetEnumOutput,
    errors: [ UnknownServerError ]
}

structure GetEnumInput {
    @required
    @httpLabel
    aa: TheEnum
}

structure GetEnumOutput {
    result: String
}

enum TheEnum {
    V1 = "v1"
    V2 = "v2"
}

@readonly
@http(method: "GET", uri: "/get-int-enum/{aa}", code: 200)
operation GetIntEnum {
    input := {
        @required
        @httpLabel
        aa: EnumResult
    }
    output := {
        @required
        result: EnumResult
    }
    errors: [ UnknownServerError ]
}

intEnum EnumResult {
    FIRST = 1
    SECOND = 2
}

@readonly
@http(method: "GET", uri: "/custom-code/{code}", code: 200)
operation CustomCode {
    input: CustomCodeInput,
    output: CustomCodeOutput,
    errors: [ UnknownServerError ]
}

structure CustomCodeInput {
    @httpLabel
    @required
    code: Integer
}

structure CustomCodeOutput {
    @httpResponseCode
    code: Integer
}