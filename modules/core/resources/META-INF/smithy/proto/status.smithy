$version: "2"

namespace alloy.proto

structure ProtobufAny {
    @protoIndex(1)
    typeUrl: String

    @protoIndex(2)
    value: Blob
}

list ProtobufAnyList {
    member: ProtobufAny
}

structure GoogleRpcStatus {
    @protoIndex(1)
    code: Integer

    @protoIndex(2)
    message: String

    @protoIndex(3)
    details: ProtobufAnyList
}
