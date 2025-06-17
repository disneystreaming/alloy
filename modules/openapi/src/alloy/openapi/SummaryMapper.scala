package alloy.openapi

import software.amazon.smithy.model.traits.Trait
import software.amazon.smithy.openapi.fromsmithy.{Context, OpenApiMapper}
import software.amazon.smithy.model.shapes.OperationShape
import software.amazon.smithy.model.traits.Trait
import software.amazon.smithy.openapi.fromsmithy.Context
import software.amazon.smithy.openapi.fromsmithy.OpenApiMapper
import software.amazon.smithy.openapi.model.OperationObject

class SummaryMapper() extends OpenApiMapper {

  override def updateOperation(
                                context: Context[_ <: Trait],
                                shape: OperationShape,
                                operation: OperationObject,
                                httpMethodName: String,
                                path: String
                              ): OperationObject ={
    if (shape.hasTrait(classOf[SummaryTrait])) {
     val summary = shape.expectTrait(classOf[SummaryTrait]).getSummary
      operation.toBuilder.summary(summary)
        .build()
    } else {
      operation
    }
  }
}
