package alloy.openapi

import software.amazon.smithy.jsonschema.Schema
import software.amazon.smithy.model.Model
import software.amazon.smithy.model.knowledge.HttpBinding
import software.amazon.smithy.model.shapes.Shape
import software.amazon.smithy.model.shapes.ShapeId
import software.amazon.smithy.model.shapes.StructureShape
import software.amazon.smithy.model.traits.TimestampFormatTrait
import software.amazon.smithy.openapi.OpenApiConfig
import software.amazon.smithy.openapi.fromsmithy.Context
import software.amazon.smithy.openapi.fromsmithy.protocols.AlloyAbstractRestProtocol

import scala.jdk.CollectionConverters._

//port of package software.amazon.smithy.openapi.fromsmithy.protocols.AwsRestJson1Protocol
class OpenApiProtocolTest extends AlloyAbstractRestProtocol[TestJsonTrait] {

  override def getProtocolType(): Class[TestJsonTrait] =
    classOf[TestJsonTrait]

  def getDocumentMediaType(): String = "application/json"

  override def updateDefaultSettings(
      model: Model,
      config: OpenApiConfig
  ): Unit = {
    config.setUseJsonName(true);
    config.setDefaultTimestampFormat(TimestampFormatTrait.Format.DATE_TIME);
  }

  def createDocumentSchema(
      context: Context[TestJsonTrait],
      shape: Shape,
      bindings: List[HttpBinding],
      mt: AlloyAbstractRestProtocol.MessageType
  ): Schema =
    if (bindings.isEmpty) Schema.builder().`type`("object").build()
    else {

      // We create a synthetic structure shape that is passed through the
      // JSON schema converter. This shape only contains members that make
      // up the "document" members of the input/output/error shape.
      val container: ShapeId = bindings.head.getMember().getContainer()
      val containerShape =
        context.getModel().expectShape(container, classOf[StructureShape])

      // Path parameters of requests are handled in "parameters" and headers are
      // handled in headers, so this method must ensure that only members that
      // are sent in the document payload are present in the structure when it is
      // converted to OpenAPI. This ensures that any path parameters are removed
      // before converting the structure to a synthesized JSON schema object.
      // Doing this sanitation after converting the shape to JSON schema might
      // result in things like "required" properties pointing to members that
      // don't exist
      val documentMemberNames = bindings.map(_.getMemberName()).toSet

      val containershapeBuilder = containerShape.toBuilder()
      for (
        memberName <- containerShape.getAllMembers().keySet().asScala
        if !documentMemberNames(memberName)
      ) {
        containershapeBuilder.removeMember(memberName)
      }

      val cleanedShape = containershapeBuilder.build()
      context
        .getJsonSchemaConverter()
        .convertShape(cleanedShape)
        .getRootSchema()
    }

}
