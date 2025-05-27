$version: "2"

namespace datetime

use alloy#LocalDate
use alloy#LocalTime
use alloy#LocalDateTime
use alloy#OffsetDateTime
use alloy#OffsetTime
use alloy#ZoneId
use alloy#ZoneOffset
use alloy#ZonedDateTime
use alloy#Year
use alloy#YearMonth
use alloy#MonthDay
use alloy#simpleRestJson

@simpleRestJson
service DateTimeService {
  operations: [DateTimeOp]
}

@http(method: "GET", uri: "/datetime")
@readonly
operation DateTimeOp {
  output := {
    out: Test
  }
}

structure Test {
  localDate: LocalDate
  localTime: LocalTime
  localDateTime: LocalDateTime
  offsetDateTime: OffsetDateTime
  offsetTime: OffsetTime
  zoneId: ZoneId
  zoneOffset: ZoneOffset
  zonedDateTime: ZonedDateTime
  year: Year
  yearMonth: YearMonth
  monthDay: MonthDay
}
