/* Copyright 2022 Disney Streaming
 *
 * Licensed under the Tomorrow Open Source Technology License, Version 1.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    https://disneystreaming.github.io/TOST-1.0.txt
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
