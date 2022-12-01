$version: "2"

namespace alloy

use alloy#simpleRestJson
use alloy#AddMenuItem
use smithy.test#httpRequestTests
use smithy.test#httpResponseTests


apply AddMenuItem @httpRequestTests([
    {
        id: "AddMenuItem",
        documentation: "add menu item tests"
        protocol: simpleRestJson,
        method: "POST"
        uri: "/restaurant/bobs/menu/item"
        headers: {
            "Content-Type": "application/json",
        },
        body: """
                {"food":{"pizza": {"name":"margharita","base":"T","toppings":["Mushroom","Tomato"]}},
                "price":9.0}
            """
        params: {
            menuItem: {
                food: {
                    pizza: {
                    name: "margharita",
                    base: "T",
                    toppings: ["MUSHROOM", "TOMATO"]
                    }
                },
                price: 9.0
            }
            restaurant: "bobs"
        }
        bodyMediaType: "application/json",
    }
])

apply AddMenuItem @httpResponseTests([
    {
        id: "AddMenuItemResult",
        protocol: simpleRestJson,
        documentation: "add menu item response tests",
        code: 201,
        headers: {
            "Content-Type": "application/json",
            "X-ADDED-AT": "1576540098"
        },
        bodyMediaType: "application/json",
        body: """
            {"itemId":"1"}
            """
        params: {
            itemId: "1",
            added: 1576540098
        }
    }
])

apply PriceError @httpResponseTests([
    {
        id: "PriceError",
        protocol: simpleRestJson,
        documentation: "add menu item response tests",
        code: 400,
        headers: {
            "Content-Type": "application/json",
            "X-ADDED-AT": "1576540098"
        },
        bodyMediaType: "application/json",
        body: """
            {"message":"Price must be greater than 0"}
            """
        params: {
            message: "Price must be greater than 0"
            code: 400
        }
    }
])

