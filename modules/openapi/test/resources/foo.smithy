$version: "2"

namespace foo

use alloy#dataExamples
use alloy#discriminated
use alloy#jsonUnknown
use alloy#nullable
use alloy#simpleRestJson
use alloy#untagged

@simpleRestJson
@externalDocumentation("API Homepage": "https://www.example.com/", "API Ref": "https://www.example.com/api-ref")
service HelloWorldService {
    version: "0.0.1"
    errors: [
        GeneralServerError
    ]
    operations: [
        Greet
        GetUnion
        GetValues
        TestErrorsInExamples
    ]
}

@externalDocumentation("API Homepage 2": "https://www.example2.com/", "API Ref 2": "https://www.example2.com/api-ref")
@readonly
@http(method: "GET", uri: "/hello/{name}/{ts}")
operation Greet {
    input: Person
    output: Greeting
    errors: [
        GreetErrorOne
        GreetErrorTwo
    ]
}

@error("client")
@httpError(404)
structure GreetErrorOne {
    @httpHeader("x-error-one")
    @required
    headerOneA: String
}

@error("client")
@httpError(404)
structure GreetErrorTwo {
    @httpHeader("x-error-one")
    @required
    headerOneB: String
}

@error("client")
@httpError(404)
structure NotFound {
    @httpHeader("x-one")
    headerOne: String

    @required
    message: String
}

@error("client")
@httpError(404)
structure NotFoundTwo {
    @httpHeader("x-two")
    headerTwo: String

    @httpHeader("x-three")
    @required
    headerThree: String

    @required
    messageTwo: String
}

@examples([
    {
        title: "ONE"
        input: { in: "test input" }
        output: { out: "test output" }
    }
    {
        title: "TWO"
        input: { in: "test input two" }
        error: {
            shapeId: NotFound
            content: { message: "Not found message" }
        }
    }
    {
        title: "THREE"
        input: { in: "test input three" }
        error: {
            shapeId: NotFoundTwo
            content: { messageTwo: "Not found message two", headerThree: "test" }
        }
    }
])
@http(method: "POST", uri: "/test_errors")
operation TestErrorsInExamples {
    input := {
        @required
        in: String
    }

    output := {
        @required
        out: String
    }

    errors: [
        NotFound
        NotFoundTwo
    ]
}

@readonly
@http(method: "GET", uri: "/default")
operation GetUnion {
    output: GetUnionResponse
}

@readonly
@http(method: "GET", uri: "/values")
operation GetValues {
    output: ValuesResponse
}

structure Person {
    @httpLabel
    @required
    name: String

    @httpHeader("X-Bamtech-Partner")
    partner: String

    @httpHeader("when")
    when: Timestamp

    @httpHeader("whenAlso")
    @timestampFormat("http-date")
    whenTwo: Timestamp

    @httpHeader("whenThree")
    @timestampFormat("date-time")
    whenThree: Timestamp

    @httpHeader("whenFour")
    @timestampFormat("epoch-seconds")
    whenFour: Timestamp

    @httpQuery("from")
    from: Timestamp

    @httpLabel
    @required
    ts: Timestamp
}

structure Greeting {
    @required
    @httpPayload
    message: String
}

@error("server")
@httpError(500)
structure GeneralServerError {
    message: String

    @nullable
    count: Integer
}

structure GetUnionResponse {
    intOrString: IntOrString
    doubleOrFloat: DoubleOrFloat
    catOrDog: CatOrDog
    catOrDogOpen: CatOrDogOpen
    catOrDogOpenDiscriminated: CatOrDogOpenDiscriminated
    vehicle: Vehicle
}

union IntOrString {
    int: Integer
    string: String
}

union DoubleOrFloat {
    float: Float
    double: Double
}

@dataExamples([
    {
        smithy: { name: "Meow" }
    }
])
structure Cat {
    name: String
}

@dataExamples([
    {
        json: { name: "Woof" }
    }
])
structure Dog {
    name: String

    breed: String

    @jsonUnknown
    attributes: Attributes
}

map Attributes {
    key: String
    value: Document
}

@dataExamples([
    {
        string: "{\"values\": []}"
    }
])
structure ValuesResponse {
    values: Values
}

list Values {
    member: SomeValue
}

@untagged
union SomeValue {
    message: String
    value: Integer
}

@discriminated("type")
@externalDocumentation(Homepage: "https://www.example.com/", "API Reference": "https://www.example.com/api-ref")
union CatOrDog {
    cat: Cat
    dog: Dog
}

union CatOrDogOpen {
    cat: Cat

    dog: Dog

    @jsonUnknown
    other: Document
}

@discriminated("type")
union CatOrDogOpenDiscriminated {
    cat: Cat

    dog: Dog

    @jsonUnknown
    other: Document
}

structure VehicleCar {
    year: Integer
    model: String
    make: String
}

structure VehiclePlane {
    model: String
}

@discriminated("type")
union Vehicle {
    car: VehicleCar
    plane: VehiclePlane
}
