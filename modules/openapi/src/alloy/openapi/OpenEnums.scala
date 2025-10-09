package alloy.openapi

import _root_.software.amazon.smithy.jsonschema.JsonSchemaConfig
import _root_.software.amazon.smithy.jsonschema.JsonSchemaMapper
import _root_.software.amazon.smithy.jsonschema.Schema.Builder
import _root_.software.amazon.smithy.model.shapes.Shape
import alloy.OpenEnumTrait
import software.amazon.smithy.model.node.ArrayNode
import scala.jdk.OptionConverters.*
import scala.jdk.CollectionConverters.*
import software.amazon.smithy.model.shapes.EnumShape
import software.amazon.smithy.model.shapes.IntEnumShape

class OpenEnums() extends JsonSchemaMapper {

  private val extensonName = "x-extensible-enum"

  override def updateSchema(
      shape: Shape,
      schemaBuilder: Builder,
      config: JsonSchemaConfig
  ): Builder = {
    val isOpen = shape.hasTrait(classOf[OpenEnumTrait]) && config
      .getExtensions(classOf[OpenApiConfigExtension])
      .getEnableMultipleExamples()
    val extensibleEnumValues = shape match {
      case stringEnum: EnumShape if isOpen =>
        val values = stringEnum.getEnumValues().asScala.values
        Some(
          values
            .foldLeft(ArrayNode.builder()) { case (arr, elem) =>
              arr.withValue(elem)
            }
            .build()
        )
      case intEnum: IntEnumShape if isOpen =>
        val values = intEnum.getEnumValues().asScala.values
        Some(
          values
            .foldLeft(ArrayNode.builder()) { case (arr, elem) =>
              arr.withValue(elem)
            }
            .build()
        )
      case _ => None
    }

    extensibleEnumValues
      .map(values =>
        schemaBuilder
          .enumValues(null)
          .intEnumValues(null)
          .putExtension(
            extensonName,
            values
          )
      )
      .getOrElse(schemaBuilder)
  }

}
