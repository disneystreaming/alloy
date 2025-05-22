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
string Date


/// This trait indicates that a Timestamp should retain the time offset
/// information as defined in RFC3339 Section 5.6.
/// See: https://www.rfc-editor.org/rfc/rfc3339#section-5.6
///
/// Must be combined with the @timestampFormat("date-time") trait as only this
/// format provides offset information.
@trait(selector: ":test(timestamp, member > timestamp) [trait|timestampFormat = 'date-time']")
structure withTimeOffset { }

@withTimeOffset
@timestampFormat("date-time")
timestamp OffsetTimestamp
