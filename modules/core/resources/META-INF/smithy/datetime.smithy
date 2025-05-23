$version: "2"

namespace alloy

/// This trait indicates that a String value contains a date without
/// a time component. Following the RFC-3339 (an extension of ISO 8601),
/// the default for a date is the following:
/// date-fullyear   = 4DIGIT
/// date-month      = 2DIGIT  ; 01-12
/// date-mday       = 2DIGIT  ; 01-28, 01-29, 01-30, 01-31 based on
///                           ; month/year
/// full-date       = date-fullyear "-" date-month "-" date-mday
///
/// See: https://www.rfc-editor.org/rfc/rfc3339#section-5.6
/// e.g.: "2022-03-30"
/// If a time component is required, you can use smithy.api#Timestamp
@trait(selector: ":test(string, member > string)")
structure dateFormat { }

@dateFormat
string LocalDate


/// This trait indicates that a String contains a time value without a timezone
/// component in the ISO-8601 format HH:MM:SS[.ss{1,9}]
/// Fractional seconds support a precision up to the nanosecond
@trait(selector: ":test(string, member > string)")
structure localTimeFormat { }

@localTimeFormat
string LocalTime


/// This trait indicates that a String contains a date-time value without a
/// timezone component in the ISO-8601 format YYYY-mm-DDTHH:MM:SS[.s{1,9}]
/// Fractional seconds support a precision up to the nanosecond.
@trait(selector: ":test(string, member > string)")
structure localDateTimeFormat { }

@localDateTimeFormat
string LocalDateTime


/// This trait indicates that a Timestamp should retain the time offset
/// information as defined in RFC3339 Section 5.6.
/// See: https://www.rfc-editor.org/rfc/rfc3339#section-5.6
///
/// Must be combined with the @timestampFormat("date-time") trait as only this
/// format provides offset information.
@trait(selector: ":test(timestamp, member > timestamp) [trait|timestampFormat = 'date-time']")
structure offsetDateTimeFormat { }

@offsetDateTimeFormat
@timestampFormat("date-time")
timestamp OffsetDateTime


/// This trait indicates that a String contains a time value with an offset in
/// the ISO-8601 format HH:MM:SS[.s{1,9}](+|-)HH:MM
/// Fractional seconds support a precision up to the nanosecond.
@trait(selector: ":test(string, member > string)")
structure offsetTimeFormat { }

@offsetTimeFormat
string OffsetTime


/// This trait indicates that a String contains a timezone identifier such as
/// America/New_York or an offset from UTC such as +01:00
@trait(selector: ":test(string, member > string)")
structure zoneIdFormat { }

@zoneIdFormat
string ZoneId


/// This triat indicates that a String contains a timezone offset from UTC
/// such as +01:00
@trait(selector: ":test(string, member > string)")
structure zoneOffsetFormat { }

@zoneOffsetFormat
string ZoneOffset


/// This trait indicates that a String contains a time value with an offset and
/// a timezone identifier in the ISO-8601 format
/// YYYY-mm-DDTHH:MM:SS[.s{1,9}](+|-)HH:MM\[ZONEID]
/// Fractional seconds support a precision up to the nanosecond.
@trait(selector: ":test(string, member > string)")
structure zonedDateTimeFormat { }

@zonedDateTimeFormat
string ZonedDateTime

enum DayOfWeek {
  MONDAY
  TUESDAY
  WEDNESDAY
  THURSDAY
  FRIDAY
  SATURDAY
  SUNDAY
}


enum Month {
  JANUARY
  FEBRUARY
  MARCH
  APRIL
  MAY
  JUNE
  JULY
  AUGUST
  SEPTEMBER
  OCTOBER
  NOVEMBER
  DECEMBER
}


/// A year in the ISO-8601 format.
@trait(selector: ":test(integer, member > integer)")
structure yearFormat { }

@yearFormat
integer Year


/// This trait indicates that a String contains a value describng a year and
/// month in the ISO-8601 format.
/// Example: 2025-05 reads as May of year 2025
@trait(selector: ":test(string, member > string)")
structure yearMonthFormat { }

@yearMonthFormat
string YearMonth


/// This trait indicates that a String contains a describint a month and day
/// in the ISO-8601 format mm-DD
/// Example: "05-14" reads as May 14th
@trait(selector: ":test(string, member > string)")
structure monthDayFormat { }

@monthDayFormat
string MonthDay


/// This trait indicates that a String contains a time duration value based on
/// the ISO-8601 duration format PnDTnHnMn.nS with days considered to be
/// exactly 24 hours.
/// For full format details see https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/time/Duration.html#parse(java.lang.CharSequence)
///
/// Examples:
/// * "PT15.784S" reads as 15 seconds and 784 milliseconds
/// * "PT25M" reads as 25 minutes
/// * "PT6H" reads as 6 hours
/// * "PT-6H3M" reads as -6 hours and +3 minutes
/// * "-PT6H3M" reads as -6 hours and -3 minutes
/// * "-PT-6H+3M" reads as +6 hours and -3 minutes
@trait(selector: ":test(string, member > string)")
structure durationFormat { }

@durationFormat
string Duration


/// This trait indicates that a String contains a period value in the format
/// PnYnMnD and PnW
/// For full format details see https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/time/Period.html#parse(java.lang.CharSequence)
///
/// Examples:
/// * "P2Y" reads as 2 years
/// * "P3W" reads as 3 weeks
/// * "P1Y2M3D" reads as 1 year, 2 months and 3 days
/// * "P-1Y2M" reads as -1 year and +2 months
/// * "-P1Y2M" reads as -1 year and -2 months
@trait(selector: ":test(string, member > string)")
structure periodFormat { }

@periodFormat
string Period


