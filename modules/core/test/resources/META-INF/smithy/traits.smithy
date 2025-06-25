$version: "2"

namespace alloy.test

use alloy#dataExamples
use alloy#dateFormat
use alloy#defaultValue
use alloy#discriminated
use alloy#nullable
use alloy#openEnum
use alloy#simpleRestJson
use alloy#structurePattern
use alloy#uncheckedExamples
use alloy#jsonUnknown
use alloy#untagged
use alloy#urlFormFlattened
use alloy#urlFormName
use alloy#uuidFormat
use alloy#offsetDateTimeFormat
use alloy.common#countryCodeFormat
use alloy.common#emailFormat
use alloy.common#hexColorCodeFormat
use alloy.common#languageCodeFormat
use alloy.common#languageTagFormat
use alloy.openapi#openapiExtensions
use alloy.openapi#summary
use alloy.proto#grpc
use alloy.proto#protoEnabled
use alloy.proto#protoIndex
use alloy.proto#protoInlinedOneOf
use alloy.proto#protoNumType
use alloy.proto#protoTimestampFormat
use alloy.proto#protoCompactUUID
use alloy.proto#protoCompactLocalDate
use alloy.proto#protoCompactYearMonth
use alloy.proto#protoCompactMonthDay
use alloy.proto#protoCompactOffsetDateTime
use alloy.proto#protoWrapped
use alloy.proto#protoReservedFields
use alloy#localTimeFormat
use alloy#localDateTimeFormat
use alloy#offsetTimeFormat
use alloy#zoneIdFormat
use alloy#zoneOffsetFormat
use alloy#zonedDateTimeFormat
use alloy#yearFormat
use alloy#yearMonthFormat
use alloy#monthDayFormat
use alloy#UUID
use alloy#LocalDate
use alloy#YearMonth
use alloy#MonthDay
use alloy#OffsetDateTime

@dateFormat
string MyDate

@dateFormat
@protoCompactLocalDate
string MyCompactDate

@emailFormat
@protoWrapped
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
@protoCompactUUID
string MyUUID

structure SomeStruct {
    @defaultValue("a")
    @nullable
    withDef: String
}

@openapiExtensions("x-foo": "bar")
list StringList {
    member: String
}

@discriminated("kind")
union Thing {
    a: MyA
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

@uncheckedExamples([{
    title: "dummy"
    input: {
        name: "john"
    }
}])
@summary("Get the age of a person")
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
@protoReservedFields([{
    number: 2
}])
structure ProtoStruct {
    @protoIndex(1)
    @protoNumType("SIGNED")
    age: Integer
}

structure ProtoStructTwo {
    @protoTimestampFormat("EPOCH_MILLIS")
    test: Timestamp
}

@untagged
union UntaggedUnion {
    a: Integer
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
    a: String
    b: Integer
}

@dataExamples([{
    smithy: {
        one: "numberOne"
        two: 2
    }
}, {
    smithy: {
        one: "numberOneAgain"
        two: 22
    }
}])
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

@structurePattern(pattern: "{test}__{test2}", target: TestStructureTarget)
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
@enum([{
    value: "A"
    name: "A"
}])
string TestOpenEnumTraitEnum

structure TestUrlFormFlattened {
    @urlFormFlattened
    test: StringList
}

structure TestUrlFormName {
    @urlFormName("Test")
    test: String
}

structure TestJsonUnknown {
   foo: String
   bar: String
   @jsonUnknown
   bazes: UnknownProps
}

map UnknownProps {
    key: String
    value: Document
}

@localTimeFormat
string MyLocalTime

@localDateTimeFormat
string MyLocalDateTime

@offsetDateTimeFormat
@timestampFormat("date-time")
timestamp MyOffsetDateTime

@offsetDateTimeFormat
@timestampFormat("date-time")
@protoCompactOffsetDateTime
timestamp MyCompactOffsetDateTime

@offsetTimeFormat
string MyOffsetTime

@zoneIdFormat
string MyZoneId

@zoneOffsetFormat
string MyZoneOffset

@zonedDateTimeFormat
string MyZonedDateTime

@yearFormat
integer MyYear

@yearMonthFormat
string MyYearMonth

@yearMonthFormat
@protoCompactYearMonth
string MyCompactYearMonth

@monthDayFormat
string MyMonthDay

@monthDayFormat
@protoCompactMonthDay
string MyCompactMonthDay

structure ProtoCompactStruct {
    @protoCompactUUID
    uuid: UUID

    @protoCompactLocalDate
    localDate: LocalDate

    @protoCompactYearMonth
    yearMonth: YearMonth

    @protoCompactMonthDay
    monthDay: MonthDay

    @protoCompactOffsetDateTime
    offsetDateTime: OffsetDateTime
}
