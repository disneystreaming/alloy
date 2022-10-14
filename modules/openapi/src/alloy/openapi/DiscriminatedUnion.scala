package alloy.openapi

import _root_.software.amazon.smithy.jsonschema.JsonSchemaConfig
import _root_.software.amazon.smithy.jsonschema.JsonSchemaMapper
import _root_.software.amazon.smithy.jsonschema.Schema.Builder
import _root_.software.amazon.smithy.model.shapes.Shape
import alloy.DiscriminatedUnionTrait
import software.amazon.smithy.jsonschema.Schema
import software.amazon.smithy.model.node.ObjectNode

import scala.jdk.CollectionConverters._

class DiscriminatedUnions() extends JsonSchemaMapper {
  private final val COMPONENTS = "components"

  def updateSchema(
      shape: Shape,
      schemaBuilder: Builder,
      config: JsonSchemaConfig
  ): Builder = {
    val maybeDiscriminated = shape.getTrait(classOf[DiscriminatedUnionTrait])
    if (maybeDiscriminated.isPresent()) {
      val discriminated = maybeDiscriminated.get()
      val discriminatorField = discriminated.getValue()
      val unionSchema = schemaBuilder.build()

      val alternatives = unionSchema.getOneOf().asScala
      val discriminatedAlts =
        alternatives.flatMap(alt => alt.getProperties().asScala)

      val transformedAlts = discriminatedAlts.map { case (label, altSchema) =>
        val localDiscriminator = Schema
          .builder()
          .`type`("string")
          .enumValues(List(label).asJava)
          .build()
        val discObject = Schema
          .builder()
          .`type`("object")
          .properties(
            Map(
              discriminatorField -> localDiscriminator
            ).asJava
          )
          .required(List(discriminatorField).asJava)
          .build()
        Schema
          .builder()
          .allOf(
            List(altSchema, discObject).asJava
          )
          .build()
      }.asJava

      schemaBuilder
        .oneOf(transformedAlts)
        .putExtension(
          "discriminator",
          ObjectNode
            .builder()
            .withMember("propertyName", discriminated.getValue())
            .build()
        )

    } else schemaBuilder
  }
}
