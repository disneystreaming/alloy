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

import scala.jdk.CollectionConverters._
import software.amazon.smithy.model.traits.ExternalDocumentationTrait
import software.amazon.smithy.model.node.ObjectNode
import software.amazon.smithy.openapi.fromsmithy.OpenApiMapper
import software.amazon.smithy.openapi.fromsmithy.Context
import software.amazon.smithy.openapi.model.OpenApi
import software.amazon.smithy.model.shapes.OperationShape
import software.amazon.smithy.openapi.model.OperationObject
import software.amazon.smithy.model.traits.Trait
import software.amazon.smithy.openapi.model.ExternalDocumentation

class ExternalDocumentationMapperOpenApi() extends OpenApiMapper {

  override def after(context: Context[_ <: Trait], openapi: OpenApi): OpenApi =
    if (context.getService.hasTrait(classOf[ExternalDocumentationTrait])) {
      context.getService
        .expectTrait(classOf[ExternalDocumentationTrait])
        .getUrls()
        .asScala
        .headOption match {
        case Some((name, url)) =>
          openapi
            .toBuilder()
            .extensions(
              ObjectNode
                .builder()
                .withMember(
                  "externalDocs",
                  ObjectNode
                    .builder()
                    .withMember("description", name)
                    .withMember("url", url)
                    .build()
                )
                .build()
            )
            .build()
        case _ => openapi
      }
    } else openapi

  override def updateOperation(
      context: Context[_ <: Trait],
      shape: OperationShape,
      operation: OperationObject,
      httpMethodName: String,
      path: String
  ): OperationObject =
    if (shape.hasTrait(classOf[ExternalDocumentationTrait])) {
      shape
        .expectTrait(classOf[ExternalDocumentationTrait])
        .getUrls()
        .asScala
        .headOption match {
        case Some((name, url)) =>
          operation
            .toBuilder()
            .externalDocs(
              ExternalDocumentation.builder.description(name).url(url).build()
            )
            .build()
        case _ => operation
      }
    } else operation

}

class ExternalDocumentationMapperJsonSchema() extends JsonSchemaMapper {

  override def updateSchema(
      shape: Shape,
      schemaBuilder: Builder,
      config: JsonSchemaConfig
  ): Builder = if (shape.hasTrait(classOf[ExternalDocumentationTrait])) {
    shape
      .expectTrait(classOf[ExternalDocumentationTrait])
      .getUrls()
      .asScala
      .headOption match {
      case Some((name, url)) =>
        val res = ObjectNode
          .builder()
          .withMember("description", name)
          .withMember("url", url)
          .build()
        schemaBuilder.putExtension("externalDocs", res)
      case _ => schemaBuilder
    }
  } else schemaBuilder
}
