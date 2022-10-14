package alloy.openapi

import _root_.software.amazon.smithy.jsonschema.JsonSchemaConfig
import _root_.software.amazon.smithy.jsonschema.JsonSchemaMapper
import _root_.software.amazon.smithy.jsonschema.Schema.Builder
import _root_.software.amazon.smithy.model.shapes.Shape
import alloy.UntaggedUnionTrait

import scala.jdk.CollectionConverters._

class UntaggedUnions() extends JsonSchemaMapper {
  private final val COMPONENTS = "components"

  def updateSchema(
      shape: Shape,
      schemaBuilder: Builder,
      config: JsonSchemaConfig
  ): Builder = if (shape.hasTrait(classOf[UntaggedUnionTrait])) {
    val unionSchema = schemaBuilder.build()

    val alternatives = unionSchema.getOneOf().asScala
    val untaggedAlts = alternatives.map { alt =>
      val untaggedAlt =
        alt
          .getProperties()
          .asScala
          .head // each alternative is an object with a single property
          ._2

      if (!untaggedAlt.getRef().isPresent()) {
        // If the alternative is not just referencing another model,
        // we annotate it with the tagged title.
        untaggedAlt
          .toBuilder()
          .title(alt.getTitle().get())
          .build()
      } else untaggedAlt
    }
    schemaBuilder.oneOf(untaggedAlts.asJava)
  } else schemaBuilder
}
