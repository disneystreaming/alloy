package alloy.openapi

import software.amazon.smithy.model.shapes.ShapeId

final case class OpenApiConversionResult(
    protocol: ShapeId,
    serviceId: ShapeId,
    contents: String
)
