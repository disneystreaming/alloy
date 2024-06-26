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

import scala.jdk.CollectionConverters._
import software.amazon.smithy.openapi.fromsmithy.OpenApiMapper
import software.amazon.smithy.openapi.fromsmithy.Context
import software.amazon.smithy.openapi.model.OpenApi
import software.amazon.smithy.model.traits.Trait
import alloy.DiscriminatedUnionTrait
import software.amazon.smithy.jsonschema.Schema
import software.amazon.smithy.model.shapes.ShapeId
import software.amazon.smithy.model.shapes.Shape
import software.amazon.smithy.model.traits.JsonNameTrait
import software.amazon.smithy.model.node.ObjectNode
import software.amazon.smithy.jsonschema.Schema.Builder

/** Creates components for the discriminated union
  */
class DiscriminatedUnionMemberComponents() extends OpenApiMapper {

  override def after(
      context: Context[_ <: Trait],
      openapi: OpenApi
  ): OpenApi = {
    val unions = context
      .getModel()
      .getUnionShapesWithTrait(classOf[DiscriminatedUnionTrait])
    val componentBuilder = openapi.getComponents().toBuilder()
    val componentSchemas: Map[String, Schema] = openapi
      .getComponents()
      .getSchemas()
      .asScala
      .toMap
    unions.asScala.foreach { union =>
      val unionMixinName = union.getId().getName() + "Mixin"
      val unionMixinId =
        ShapeId.fromParts(union.getId().getNamespace(), unionMixinName)
      val discriminatorField =
        union.expectTrait(classOf[DiscriminatedUnionTrait]).getValue()

      val unionMixinSchema = Schema
        .builder()
        .`type`("object")
        .properties(
          Map(
            discriminatorField -> Schema
              .builder()
              .`type`("string")
              .build()
          ).asJava
        )
        .required(List(discriminatorField).asJava)
        .build()

      val unionMixinRef = context.createRef(unionMixinId)

      componentBuilder.putSchema(unionMixinName, unionMixinSchema)

      union.members().asScala.foreach { memberShape =>
        val syntheticMemberName =
          union.getId().getName() + memberShape.getMemberName.capitalize
        context.getPointer(union).split('/').last + memberShape
          .getMemberName()
          .capitalize
        val targetRef = context.createRef(memberShape.getTarget())
        val syntheticUnionMember =
          Schema.builder().allOf(List(targetRef, unionMixinRef).asJava).build()
        componentBuilder.putSchema(syntheticMemberName, syntheticUnionMember)
      }

      val existingSchemaBuilder = componentSchemas
        .get(union.toShapeId.getName)
        .map(_.toBuilder())
        .getOrElse(Schema.builder())
      componentBuilder.putSchema(
        union.toShapeId.getName,
        updateDiscriminatedUnion(
          union,
          existingSchemaBuilder,
          discriminatorField
        )
          .build()
      )

    }
    openapi.toBuilder.components(componentBuilder.build()).build()
  }

  private def updateDiscriminatedUnion(
      shape: Shape,
      schemaBuilder: Builder,
      discriminatorField: String
  ): Builder = {
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
          .withMember("propertyName", discriminatorField)
          .withMember("mapping", mapping)
          .build()
      )
  }

}
