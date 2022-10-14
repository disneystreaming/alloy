package alloy.openapi

import software.amazon.smithy.model.shapes.OperationShape
import software.amazon.smithy.model.traits.TagsTrait
import software.amazon.smithy.model.traits.Trait
import software.amazon.smithy.openapi.fromsmithy.Context
import software.amazon.smithy.openapi.fromsmithy.OpenApiMapper
import software.amazon.smithy.openapi.model.OperationObject

import scala.jdk.CollectionConverters._

class AddTags() extends OpenApiMapper {

  override def postProcessOperation(
      context: Context[_ <: Trait],
      shape: OperationShape,
      operation: OperationObject,
      httpMethodName: String,
      path: String
  ): OperationObject = {
    val maybeTags = shape.getTrait(classOf[TagsTrait])
    val builder = operation.toBuilder()
    if (maybeTags.isPresent()) {
      maybeTags
        .get()
        .getValues()
        .asScala
        .foreach(builder.addTag)
    }
    builder.build()
  }

}
