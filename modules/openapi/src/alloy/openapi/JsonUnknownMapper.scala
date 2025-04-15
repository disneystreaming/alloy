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
import software.amazon.smithy.jsonschema.Schema.Builder
import software.amazon.smithy.model.node.Node
import alloy.JsonUnknownTrait

import scala.jdk.CollectionConverters._
import scala.jdk.OptionConverters.*
import software.amazon.smithy.jsonschema.JsonSchemaMapperContext
import alloy.DiscriminatedUnionTrait
import software.amazon.smithy.jsonschema.Schema

class JsonUnknownMapper() extends JsonSchemaMapper {
  private final val ADDITIONAL_PROPERTIES = "additionalProperties"

  override def updateSchema(
      context: JsonSchemaMapperContext,
      schemaBuilder: Builder
  ): Builder = {
    val shape = context.getShape()

    if (!shape.members.asScala.exists(_.hasTrait(classOf[JsonUnknownTrait])))
      schemaBuilder
    else {
      val unknownMember = shape
        .members()
        .asScala
        .find(_.hasTrait(classOf[JsonUnknownTrait]))
        .getOrElse(
          sys.error("Didn't find an unknown member, even though we just did")
        )

      if (shape.isStructureShape()) {
        schemaBuilder
          .removeProperty(unknownMember.getMemberName)
          .additionalProperties(Schema.fromNode(Node.from(true)))
      } else if (
        shape
          .isUnionShape() && !shape.hasTrait(classOf[DiscriminatedUnionTrait])
      ) {
        val b = schemaBuilder.build()
        schemaBuilder.oneOf(
          b.getOneOf()
            .asScala
            .map {
              case member
                  if member
                    .getTitle()
                    .toScala
                    .contains(unknownMember.getMemberName()) =>
                member
                  .toBuilder()
                  .required(Nil.asJava)
                  .properties(Map.empty.asJava)
                  .additionalProperties(Schema.fromNode(Node.from(true)))
                  .build()
              case other => other
            }
            .asJava
        )
      } else
        schemaBuilder
    }
  }
}
