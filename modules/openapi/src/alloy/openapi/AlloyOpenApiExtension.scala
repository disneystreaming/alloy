package alloy.openapi

import _root_.software.amazon.smithy.jsonschema.JsonSchemaMapper
import _root_.software.amazon.smithy.openapi.fromsmithy.OpenApiJsonSchemaMapper
import _root_.software.amazon.smithy.openapi.fromsmithy.OpenApiMapper
import _root_.software.amazon.smithy.openapi.fromsmithy.OpenApiProtocol
import _root_.software.amazon.smithy.openapi.fromsmithy.Smithy2OpenApiExtension
import _root_.software.amazon.smithy.openapi.fromsmithy.mappers._

import java.{util => ju}
import scala.jdk.CollectionConverters._

final class AlloyOpenApiExtension() extends Smithy2OpenApiExtension {

  override def getProtocols(): ju.List[OpenApiProtocol[_]] = List(
    new AlloyOpenApiProtocol()
  ).asJava.asInstanceOf[ju.List[OpenApiProtocol[_]]]

  override def getOpenApiMappers(): ju.List[OpenApiMapper] = List(
    new CheckForGreedyLabels(),
    new CheckForPrefixHeaders(),
    new OpenApiJsonSubstitutions(),
    new OpenApiJsonAdd(),
    new RemoveUnusedComponents(),
    new UnsupportedTraits(),
    new RemoveEmptyComponents(),
    new AddTags()
  ).asJava

  override def getJsonSchemaMappers(): ju.List[JsonSchemaMapper] = List(
    new OpenApiJsonSchemaMapper(): JsonSchemaMapper,
    new DiscriminatedUnions(),
    new UntaggedUnions()
  ).asJava

}
