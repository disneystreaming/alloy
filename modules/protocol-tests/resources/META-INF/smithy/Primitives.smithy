$version: "2"

namespace alloy.test

use alloy#simpleRestJson
use alloy.test#Primitives
use smithy.test#httpRequestTests
use smithy.test#httpResponseTests

apply Primitives @httpRequestTests([
    {
        id: "PrimitivesEncodingRequest"
        protocol: simpleRestJson
        uri: "/primitive/encoding"
        method: "POST"
        body: """
            {"localTime":"13:26:51.123456789","duration":86400.000000001,"uuid":"51216269-c0c8-454a-871e-329513e54e23","offsetDateTime":"2025-08-15T20:26:51Z","localDate":"2025-08-15"}"""
        params: {
            uuid: "51216269-c0c8-454a-871e-329513e54e23"
            localDate: "2025-08-15"
            localTime: "13:26:51.123456789"
            duration: 86400.000000001
            offsetDateTime: 1755289611
        }
    }
])

apply Primitives @httpResponseTests([
    {
        id: "PrimitivesEncodingResponse"
        protocol: simpleRestJson
        code: 200
        body: """
        {"localTime":"13:26:51.123456789","duration":86400.000000001,"uuid":"51216269-c0c8-454a-871e-329513e54e23","offsetDateTime":"2025-08-15T20:26:51-07:00","localDate":"2025-08-15"}"""
        params: {
            uuid: "51216269-c0c8-454a-871e-329513e54e23"
            localDate: "2025-08-15"
            localTime: "13:26:51.123456789"
            duration: 86400.000000001
            offsetDateTime: "2025-08-15T20:26:51-07:00"
        }
    }
])
