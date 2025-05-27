### Additional constraint traits

The constraints described in this document are meant to extend the set of constraints and behavioural traits provided by the smithy standard library (such as `smithy.api#pattern` or `smithy.api#length`).

Users defining new protocols/tools MAY use to implement support for these traits. In particular, [smithy-translate](https://github.com/disneystreaming/smithy-translate) makes use of these traits to capture in smithy some semantics described in openapi.

#### alloy#nullable

Out of the box, Smithy does not make a distinction between a missing value and a value set to `null`. Some other Interface Definition Languages (IDL) allow for this distinction. This trait can be used to express this distinction.

```smithy
structure Foo {
 @required
 @nullable
 bar: String
}
```

#### alloy#defaultValue

Smithy 2.0 introduces the [`@default` trait](https://smithy.io/2.0/spec/type-refinement-traits.html#default-trait) but this trait is restrictive and can't be used in some use case. For example, you can use `@defaultValue` to set a default of `"N/A"` on a `String` that's constrained with the `length` trait to a minimum of 5 characters. Smithy's `@default` trait won't allow that.

```smithy
@length(min: 5)
string MyString

structure Foo {
 @required
 @defaultValue("N/A")
 bar: MyString
}
```


### alloy#openEnum

Specifies that an enumeration is open meaning that it can accept "unknown" values that are not explicitly specified inside of the smithy enum shape definition.
This trait should be mainly be used for interop with external libraries that require it. Often a string or integer type may be more applicable if there are many different
possible values that the API can return.

This trait can be applied to `enum` or `intEnum` shapes. Additionally it can be used on String shapes with the `smithy.api#enum` trait. This is supported for backward compatibility since the `enum` constraint trait is deprecated.

```smithy
@openEnum
enum Shape {
  SQUARE, CIRCLE
}

@openEnum
intEnum IntShape {
  SQUARE = 1
  CIRCLE = 2
}
```

### alloy#structurePattern

The `alloy#structurePattern` trait provides a way to specify that a given `String` will conform to a provided format and that it should be parsed into a `Structure` rather than a `String`. For example:

```smithy
@structurePattern(pattern: "{foo}_{bar}", target: FooBar)
string FooBarString

structure FooBar {
  @required
  foo: String
  @required
  bar: Integer
}
```

Now wherever `FooBarString` is used, it will really be parsing the string into the structure `FooBar`. There are a few requirements for using the `structurePattern` trait that are checked by a validator:

- The target structure must have all required members and all members must target simple shapes.
- The provided pattern must have all parameters separated by at least one character. The reason for this is that if there is no separation (e.g. "{foo}{bar}") then a parser would not be able to tell when one starts and the other begins.
- There must be a provided pattern parameter for each member of the target structure.


### Datetime constraints

#### alloy#dateFormat

This trait is used to express that a `String` in your model is formatted as a date.
The format is defined in the [RFC 3339](https://www.rfc-editor.org/rfc/rfc3339#section-5.6).

Example: `2022-12-28`.

```smithy
structure Test {
  @dateFormat
  myDate: String
}
```

For convenience alloy provides the type `alloy#Date` which is equivalent to a `String` annotated with the `dateFormat` trait.

#### alloy#localDateTimeFormat

This trait indicates that a `String` contains a date-time value without a
timezone component in the ISO-8601 format `YYYY-mm-DDTHH:MM:SS[.s{1,9}]`.

Fractional seconds support a precision up to the nanosecond.

Example: `2025-05-27T16:12:38.470046`

```smithy
structure Test {
  @localTimeFormat
  localDateTime: string
}
```

For convenience alloy provides the type `alloy#LocalDateTime` which is equivalent to a `String` annotated with the `localDateTimeFormat` trait.

#### alloy#localTimeFormat

This trait indicates that a `String` contains a time value without a timezone
component in the ISO-8601 format `HH:MM:SS[.ss{1,9}]`.

Fractional seconds support a precision up to the nanosecond.

Example: `16:12:13.475041`

```smithy
structure Test {
  @localTimeFormat
  localTime: string
}
```

For convenience alloy provides the type `alloy#LocalTime` which is equivalent to a `String` annotated with the `localTimeFormat` trait.


#### alloy#offsetDateTimeFormat

This trait is used to indicates that a `Timestamp` should retain time offset
information as defined in the [RFC 3339, section 5.6](https://www.rfc-editor.org/rfc/rfc3339#section-5.6).

This trait must be combined with `smithy.api#timestampFormat("date-time")`.

Example: `2025-05-27T16:10:31.961242-04:00`

```smithy
structure Test {
  @offsetDateTimeFormat
  @timestampFormat("date-time")
  ts: Timestamp
}
```

For convenience alloy provides the type `alloy#OffsetDateTime` which is equivalent to a `Timestamp` annotated with the `offsetDateTimeFormat` trait.


#### alloy#offsetTimeFormat

This trait indicates that a `String` contains a time value with an offset in
the ISO-8601 format `HH:MM:SS[.s{1,9}](+|-)HH:MM`.

Fractional seconds support a precision up to the nanosecond.

Example: `16:11:19.052019-04:00`

```smithy
structure Test {
  @offsetTimeFormat
  offsetTime: string
}
```

For convenience alloy provides the type `alloy#OffestTime` which is equivalent to a `String` annotated with the `offsetTimeFormat` trait.


#### alloy#zoneIdFormat

This trait indicates that a `String` contains a timezone identifier such as
`America/New_York` or an offset from UTC such as `+01:00`.

```smithy
structure Test {
  @zoneIdFormat
  zoneId: string
}
```

For convenience alloy provides the type `alloy#ZoneId` which is equivalent to a `String` annotated with the `zoneIdFormat` trait.


#### alloy#zoneOffsetFormat

This trait indicates that a `String` contains a timezone offset from UTC.

Example: `+01:00`

```smithy
structure Test {
  @zoneOffsetFormat
  zoneOffset: string
}
```

For convenience alloy provides the type `alloy#ZoneOffset` which is equivalent to a `String` annotated with the `zoneOffsetFormat` trait.


#### alloy#zonedDateTimeFormat

This trait indicates that a `String` contains a time value with an offset and
a timezone identifier in the ISO-8601 format `YYYY-mm-DDTHH:MM:SS[.s{1,9}](+|-)HH:MM\[ZONEID]`

Example: `2025-05-27T16:03:14.557546-04:00[America/New_York]`

Fractional seconds support a precision up to the nanosecond.

```smithy
structure Test {
  @zonedDateTimeFormat
  zonedDateTime: string
}
```

For convenience alloy provides the type `alloy#ZonedDateTime` which is equivalent to a `String` annotated with the `zonedDateTimeFormat` trait.


#### alloy#yearFormat

This trait indicates that an `Integer` contains a year in the ISO-8601 format.

Example: `2025`

```smithy
structure Test {
  @yearFormat
  year: Integer
}
```

For convenience alloy provides the type `alloy#Year` which is equivalent to an `Integer` annotated with the `yearFormat` trait.


#### alloy#yearMonthFormat

This trait indicates that a `String` contains a value describng a year and
month in the ISO-8601 format `YYYY-mm`.

Example: `2025-05` reads as May of year 2025

```smithy
structure Test {
  @yearMonthFormat
  yearMonth: String
}
```

For convenience alloy provides the type `alloy#YearMonth` which is equivalent to an `String` annotated with the `yearMonthFormat` trait.


#### alloy#monthDayFormat

This trait indicates that a `String` contains a describint a month and day
in the ISO-8601 format `mm-DD`

Example: `05-14` reads as May 14th

```smithy
structure Test {
  @monthDayFormat
  monthDay: String
}
```

For convenience alloy provides the type `alloy#MonthDay` which is equivalent to an `String` annotated with the `monthDayFormat` trait.
