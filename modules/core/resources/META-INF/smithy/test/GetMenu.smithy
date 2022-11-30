$version: "2"

namespace alloy.test

use alloy.test#GetMenu
use alloy#simpleRestJson
use smithy.test#httpRequestTests
use smithy.test#httpResponseTests

apply GetMenu @httprequesttests([
    {
        id: "GetMenuRequest"
        protocol: simpleRestJson
        uri: "/restaurant/unclemikes/menu"
        method: "GET"
        params: {
            restaurant: "unclemikes"
        }
    }
])


apply GetMenu @httpresponseTests([
    {
        id: "GetMenuResponse"
        protocol: simpleRestJson
        code: 200
        body: """
        {"a0b0f3a9-81d3-4bf3-8897-a76423116403" :{"food":{"pizza": {"name":"margharita","base":"T","toppings":["Mushroom","Tomato"]}},
        "price":9.0}}
        """
        params: {
            menu: {
                "a0b0f3a9-81d3-4bf3-8897-a76423116403" :{
                    food:{
                        pizza: {
                            name:"margharita",
                            base:"T",
                            toppings:["Mushroom","Tomato"]
                        }
                    },
                    price:9.0
                }
            }
        }
    }
])