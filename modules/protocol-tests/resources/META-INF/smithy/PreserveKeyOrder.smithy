$version: "2"

namespace alloy.test

use alloy#simpleRestJson
use alloy.test#PreserveOrder
use smithy.test#httpRequestTests
use smithy.test#httpResponseTests

apply PreserveOrder @httpRequestTests([
    {
        id:"PreserveKeyOrderRequest"
        protocol: simpleRestJson
        uri: "/preserveKeyOrder"
        method: "POST"
        body : """
        {"map":{"a":1,"d":2,"e":3,"b":4},"document":{"foo":1,"a":"b","c":[],"bar":null}}"""
        params: {
            map: {
                "a": 1,
                "d": 2,
                "e": 3,
                "b": 4
            },
            document: {
                "foo": 1,
                "a": "b",
                "c": [],
                "bar": null
            }
        }
    }
])

apply PreserveOrder @httpResponseTests([
    {
        id: "PreserveKeyOrderResponse"
        protocol: simpleRestJson
        uri: "/preserveKeyOrder"
        method: "POST"
        code: 200
        body : """
        {"map":{"a":1,"d":2,"e":3,"b":4},"document":{"foo":1,"a":"b","c":[],"bar":null}}"""
        params: {
            map: {
                "a": 1,
                "d": 2,
                "e": 3,
                "b": 4
            },
            document: {
                "foo": 1,
                "a": "b",
                "c": [],
                "bar": null
            }
        }
    }
])
