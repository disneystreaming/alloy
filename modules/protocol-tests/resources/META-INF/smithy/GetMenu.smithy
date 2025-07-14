$version: "2"

namespace alloy.test

use alloy.test#GetMenu
use alloy#simpleRestJson
use smithy.test#httpRequestTests
use smithy.test#httpResponseTests

apply GetMenu @httpRequestTests([
    {
        id: "GetMenuRequest"
        protocol: simpleRestJson
        uri: "/restaurant/uncle%3Amikes/menu"
        method: "GET"
        params: {
            restaurant: "uncle:mikes"
        }
    }
])

apply GetMenu @httpResponseTests([
    {
        id: "GetMenuResponse"
        protocol: simpleRestJson
        code: 200
        bodyMediaType: "application/json"
        body: """
        {"a0b0f3a9-81d3-4bf3-8897-a76423116403" :{"food":{"pizza": {"name":"margharita","base":"T","toppings":["MUSHROOM", "TOMATO"]}},"price":9.0}}
        """
        params: {
            menu: {
                "a0b0f3a9-81d3-4bf3-8897-a76423116403": {
                    food: {
                        pizza: {
                            name: "margharita",
                            base: "T",
                            toppings: ["MUSHROOM", "TOMATO"]
                        }
                    },
                    price: 9.0
                }
            }
        }
    }
])

apply NotFoundError @httpResponseTests([
    {
        id: "NotFoundError"
        protocol: simpleRestJson
        code: 404
        headers: {
            "Content-Type": "application/json",
            "X-Error-Type": "NotFoundError"
        },
        bodyMediaType: "application/json"
        body: """
        {"name":"unknown"}
        """ ,
        params: {
            name: "unknown"
        }
    }
])