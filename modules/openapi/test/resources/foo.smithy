namespace foo

use alloy#simpleRestJson
use alloy#discriminated
use alloy#untagged
use alloy#dataExamples

@simpleRestJson
@externalDocumentation(
  "API Homepage": "https://www.example.com/",
  "API Ref": "https://www.example.com/api-ref",
)
service HelloWorldService {
  version: "0.0.1",
  errors: [GeneralServerError],
  operations: [Greet, GetUnion, GetValues]
}

@externalDocumentation(
  "API Homepage 2": "https://www.example2.com/",
  "API Ref 2": "https://www.example2.com/api-ref",
)
@readonly
@http(method: "GET", uri: "/hello/{name}/{ts}")
operation Greet {
  input: Person,
  output: Greeting
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
  name: String,

  @httpHeader("X-Bamtech-Partner")
  partner: String,

  @httpHeader("when")
  when: Timestamp,

  @httpHeader("whenAlso")
  @timestampFormat("http-date")
  whenTwo: Timestamp,

  @httpHeader("whenThree")
  @timestampFormat("date-time")
  whenThree: Timestamp,

  @httpHeader("whenFour")
  @timestampFormat("epoch-seconds")
  whenFour: Timestamp,

  @httpQuery("from")
  from: Timestamp,

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
  message: String,
}

structure GetUnionResponse {
  intOrString: IntOrString,
  doubleOrFloat: DoubleOrFloat,
  catOrDog: CatOrDog
}

union IntOrString {
  int: Integer,
  string: String
}

union DoubleOrFloat {
  float: Float,
  double: Double
}

@dataExamples([{
  smithy: {
    name: "Meow"
  }
}])
structure Cat {
  name: String
}

@dataExamples([{
  json: {
    name: "Woof"
  }
}])
structure Dog {
  name: String,
  breed: String
}

@dataExamples([{
  string: "{\"values\": []}"
}])
structure ValuesResponse {
  values: Values
}

list Values {
  member: SomeValue
}

@untagged
union SomeValue {
  message: String,
  value: Integer
}

@discriminated("type")
@externalDocumentation(
  "Homepage": "https://www.example.com/",
  "API Reference": "https://www.example.com/api-ref",
)
union CatOrDog {
  cat: Cat,
  dog: Dog
}


