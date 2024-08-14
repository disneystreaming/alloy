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

import software.amazon.smithy.jsonschema.JsonSchemaMapper
import software.amazon.smithy.jsonschema.JsonSchemaConfig
import software.amazon.smithy.jsonschema.Schema.Builder
import software.amazon.smithy.model.node.Node
import software.amazon.smithy.model.shapes.Shape
import alloy.JsonUnknownTrait

import scala.jdk.CollectionConverters._

class JsonUnknownMapper() extends JsonSchemaMapper {
  private final val ADDITIONAL_PROPERTIES = "additionalProperties"

  override def updateSchema(
      shape: Shape,
      schemaBuilder: Builder,
      config: JsonSchemaConfig
  ): Builder = {
    val jsonUnknownMemberName = shape
      .getAllMembers()
      .asScala
      .collect {
        case (name, member) if member.hasTrait(classOf[JsonUnknownTrait]) =>
          name
      }
      .headOption

    jsonUnknownMemberName.fold(schemaBuilder) { name =>
      schemaBuilder
        .removeProperty(name)
        .putExtension(ADDITIONAL_PROPERTIES, Node.from(true))
    }
  }
}
