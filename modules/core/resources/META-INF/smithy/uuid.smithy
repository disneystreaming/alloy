$version: "2"

namespace alloy

/// UUID v4 compliant with [RFC 4122](https://www.rfc-editor.org/rfc/rfc4122)
@trait(selector: "string")
structure uuidFormat {}

@uuidFormat
string UUID

