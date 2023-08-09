$version: "2"

namespace alloy.test

use alloy.common#countryCodeFormat
use alloy.common#emailFormat
use alloy.common#hexColorCodeFormat
use alloy.common#languageCodeFormat
use alloy.common#languageTagFormat
use alloy.openapi#openapiExtensions
use alloy.proto#grpc
use alloy.proto#protoEnabled
use alloy.proto#protoIndex
use alloy.proto#protoInlinedOneOf
use alloy.proto#protoNumType
use alloy.proto#protoReservedFields
use alloy#dateFormat
use alloy#defaultValue
use alloy#discriminated
use alloy#nullable
use alloy#openEnum
use alloy#uncheckedExamples
use alloy#untagged
use alloy#uuidFormat
use alloy#simpleRestJson
use alloy#dataExamples
use alloy#structurePattern

@dateFormat
string MyDate
@emailFormat
string Email 
@countryCodeFormat
string CountryCode
@languageCodeFormat
string LanguageCode
@languageTagFormat
string LanguageTag
@hexColorCodeFormat
string HexColorCode
@uuidFormat
string ID

structure SomeStruct {
    @defaultValue("a")
    @nullable
    withDef: String
}

@openapiExtensions(
  "x-foo": "bar"
)
list StringList {
  member: String
}

@discriminated("kind")
union Thing {
    a: MyA,
    b: MyB
}

structure MyA {
    value: String
}
structure MyB {
    value: Integer
}

@grpc
service MyGrpcService {
    version: "1"
    operations: [GetAge]
}

@uncheckedExamples([{title: "dummy", input: { name: "john" }}])
@http(method: "GET", uri: "/age")
operation GetAge {
    input := {
        @required
        @httpHeader("X-NAME")
        name: String
    }
    output := {
        age: String
    }
}

@protoEnabled
@protoReservedFields([{number: 2}])
structure ProtoStruct {
    @protoIndex(1)
    @protoNumType("SIGNED")
    age: Integer
}

@untagged
union UntaggedUnion {
    a: Integer,
    b: String
}

@simpleRestJson
service RestJsonService {
    version: "1"
    operations: [GetAge]
}

@protoEnabled
structure UnionHost {
    value: OtherUnion
}

@protoInlinedOneOf
union OtherUnion {
    a: String,
    b: Integer
}

@dataExamples([
    {
        smithy: {
            one: "numberOne",
            two: 2
        }
    },
    {
        smithy: {
            one: "numberOneAgain",
            two: 22
        }
    }
])
structure TestExamples {
    one: String
    two: Integer
}

@dataExamples([{
    json: {
        test: "numberOne"
    }
}])
structure TestJsonExamples {
    one: String
    two: Integer
}

@dataExamples([{
    string: "test"
}])
structure TestStringExamples {
    one: String
    two: Integer
}

@structurePattern(
    pattern: "{test}__{test2}"
    target: TestStructureTarget
)
string TestStructurePattern

structure TestStructureTarget {
    @required
    test: String
    @required
    test2: String
}

@openEnum
enum TestOpenEnum {
    ONE
}

@openEnum
intEnum TestOpenIntEnum {
    ONE = 1
}

@openEnum
@enum([{value: "A", name: "A"}])
string TestOpenEnumTraitEnum
