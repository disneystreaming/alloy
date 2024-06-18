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
import alloy.DiscriminatedUnionTrait
import software.amazon.smithy.jsonschema.Schema

import scala.jdk.CollectionConverters._
import software.amazon.smithy.model.node.ObjectNode
import software.amazon.smithy.model.traits.JsonNameTrait
import alloy.openapi.OptionalExt

class DiscriminatedUnions() extends JsonSchemaMapper {

  override def updateSchema(
      shape: Shape,
      schemaBuilder: Builder,
      config: JsonSchemaConfig
  ): Builder = {
    val maybeDiscriminated = shape.getTrait(classOf[DiscriminatedUnionTrait])
    if (maybeDiscriminated.isPresent()) {
      val alts = shape
        .members()
        .asScala
        .map { member =>
          val label = member
            .getTrait(classOf[JsonNameTrait])
            .asScala
            .map(_.getValue())
            .getOrElse(member.getMemberName())
          val syntheticMemberId =
            shape.getId().getName() + member.getMemberName().capitalize
          val refString = s"#/components/schemas/$syntheticMemberId"
          val refSchema =
            Schema.builder.ref(refString).build
          (label, refString, refSchema)
        }
        .toList
      val schemas = alts.map(_._3).asJava
      val mapping = ObjectNode.fromStringMap(
        alts
          .map { case (label, refString, _) => (label, refString) }
          .toMap
          .asJava
      )
      schemaBuilder
        .oneOf(schemas)
        .putExtension(
          "discriminator",
          ObjectNode
            .builder()
            .withMember("propertyName", maybeDiscriminated.get().getValue())
            .withMember("mapping", mapping)
            .build()
        )
    } else schemaBuilder
  }
}
