$version: "2"

namespace alloy.test

use alloy#simpleRestJson
use alloy.test#AddMenuItem
use smithy.test#httpRequestTests
use smithy.test#httpResponseTests


apply AddMenuItem @httpRequestTests([
    {
        id: "AddMenuItem",
        documentation: "add menu item test"
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
                    toppings: ["Mushroom", "Tomato"]
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
        documentation: "add menu item response test",
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
    },
    {
        id: "PriceError",
        protocol: simpleRestJson,
        documentation: "price error test",
        code: 400,
        headers: { "X-CODE": "400",
            "Content-Type": "application/json",
            }
        body: """
                {"priceError" :{"message":"invalid price"}}
                """
        bodyMediaType: "application/json",
        params: {
            PriceError: {message: "invalid price", code: 400 }
        }
    }
])

