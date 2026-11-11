$version: "2"

namespace patch

use alloy#simpleRestJson

@simpleRestJson
service ThingService {
    version: "0.0.1"
    operations: [
        UpdateThing
    ]
}

@idempotent
@http(method: "PATCH", uri: "/things/{id}")
operation UpdateThing {
    input := {
        @required
        @httpLabel
        id: String
    }

    output := {
        @required
        name: String
    }

    errors: [
        Overloaded
        Unavailable
    ]
}

@error("server")
@httpError(503)
structure Overloaded {
    @httpHeader("Retry-After")
    @required
    retryAfter: Integer

    @required
    message: String
}

@error("server")
@httpError(503)
structure Unavailable {
    @httpHeader("X-Reason")
    @required
    reason: String

    @required
    message: String
}
