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

import alloy.NullableTrait
import software.amazon.smithy.jsonschema.{
  JsonSchemaMapper,
  JsonSchemaMapperContext,
  Schema
}
import software.amazon.smithy.model.node.Node

class NullableMapper extends JsonSchemaMapper {
  override def updateSchema(
      context: JsonSchemaMapperContext,
      schemaBuilder: Schema.Builder
  ): Schema.Builder = {
    context.getShape
      .getTrait(classOf[NullableTrait])
      .asScala
      .map { _ =>
        schemaBuilder.putExtension(
          "nullable",
          Node.from(true)
        )
      }
      .getOrElse(schemaBuilder)
  }
}
