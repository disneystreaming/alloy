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

    }
    openapi.toBuilder.components(componentBuilder.build()).build()
  }

}
